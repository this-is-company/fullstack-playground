package com.example.cacheapp.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(name = "app.cache.mode", havingValue = "both", matchIfMissing = true)
public class TwoLevelCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelCacheConfig.class);

    private static final Set<String> NAMES = Set.of("products", "products-list");

    @Bean
    CacheManager caffeineL1() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(NAMES);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }

    @Bean
    CacheManager redisL2(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        ObjectMapper redisMapper = objectMapper.copy();
        redisMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer values = new GenericJackson2JsonRedisSerializer(redisMapper);
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(values))
                .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .initialCacheNames(NAMES)
                .build();
    }

    @Bean
    @Primary
    CacheManager cacheManager(CacheManager caffeineL1, CacheManager redisL2) {
        log.info("CacheManager = L1 Caffeine + L2 Redis (HIT/MISS 는 TwoLevelCache 로그)");
        return new TwoLevelCacheManager(caffeineL1, redisL2);
    }
}
