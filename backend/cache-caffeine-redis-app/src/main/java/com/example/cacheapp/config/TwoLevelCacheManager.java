package com.example.cacheapp.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TwoLevelCacheManager implements CacheManager {

    private final CacheManager l1;
    private final CacheManager l2;
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

    public TwoLevelCacheManager(CacheManager l1, CacheManager l2) {
        this.l1 = l1;
        this.l2 = l2;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, n -> {
            Cache level1 = l1.getCache(n);
            Cache level2 = l2.getCache(n);
            if (level1 == null || level2 == null) {
                throw new IllegalStateException("cache not found: " + n);
            }
            return new TwoLevelCache(n, level1, level2);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return l1.getCacheNames();
    }

    public CacheManager l1() {
        return l1;
    }

    public CacheManager l2() {
        return l2;
    }
}
