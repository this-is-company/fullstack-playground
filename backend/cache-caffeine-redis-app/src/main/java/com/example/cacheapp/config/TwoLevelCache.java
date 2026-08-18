package com.example.cacheapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * L1 Caffeine → miss 이면 L2 Redis. put/evict 는 양쪽.
 */
public class TwoLevelCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelCache.class);

    private final String name;
    private final Cache l1;
    private final Cache l2;

    public TwoLevelCache(String name, Cache l1, Cache l2) {
        this.name = name;
        this.l1 = l1;
        this.l2 = l2;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper hit = l1.get(key);
        if (hit != null) {
            log.info("[CACHE HIT L1-Caffeine] name={} key={}", name, key);
            return hit;
        }
        hit = l2.get(key);
        if (hit != null) {
            log.info("[CACHE HIT L2-Redis] name={} key={} → L1 채움", name, key);
            l1.put(key, hit.get());
            return hit;
        }
        log.info("[CACHE MISS L1+L2] name={} key={} → DB", name, key);
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper == null) {
            return null;
        }
        return type.cast(wrapper.get());
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            T value = (T) wrapper.get();
            return value;
        }
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        log.info("[CACHE PUT L1+L2] name={} key={}", name, key);
        l1.put(key, value);
        l2.put(key, value);
    }

    @Override
    public void evict(Object key) {
        log.info("[CACHE EVICT L1+L2] name={} key={}", name, key);
        l1.evict(key);
        l2.evict(key);
    }

    public void evictL1Only(Object key) {
        log.info("[CACHE EVICT L1 only] name={} key={} (L2 Redis 유지)", name, key);
        l1.evict(key);
    }

    @Override
    public void clear() {
        l1.clear();
        l2.clear();
    }

    public Cache l1() {
        return l1;
    }

    public Cache l2() {
        return l2;
    }
}
