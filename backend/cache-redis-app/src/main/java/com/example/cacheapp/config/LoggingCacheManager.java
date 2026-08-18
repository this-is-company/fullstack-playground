package com.example.cacheapp.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final String store;
    private final ConcurrentHashMap<String, Cache> wrappers = new ConcurrentHashMap<>();

    public LoggingCacheManager(CacheManager delegate, String store) {
        this.delegate = delegate;
        this.store = store;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = delegate.getCache(name);
        if (cache == null) {
            return null;
        }
        return wrappers.computeIfAbsent(name, n -> new LoggingCache(cache, store));
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
