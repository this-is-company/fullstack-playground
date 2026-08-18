package com.example.cacheapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(name = "app.cache.mode", havingValue = "caffeine")
public class CaffeineOnlyCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CaffeineOnlyCacheConfig.class);

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("products", "products-list");
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        log.info("CacheManager = Caffeine only (app.cache.mode=caffeine)");
        return new LoggingCacheManager(manager, "Caffeine");
    }
}
