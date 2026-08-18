package com.example.cacheapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/** hit 이면 메서드/SQL 없이 여기만 찍힌다. */
public class LoggingCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(LoggingCache.class);

    private final Cache delegate;
    private final String store;

    public LoggingCache(Cache delegate, String store) {
        this.delegate = delegate;
        this.store = store;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper value = delegate.get(key);
        if (value != null) {
            log.info("[CACHE HIT {}] name={} key={} value={}", store, getName(), key, value.get());
        } else {
            log.info("[CACHE MISS {}] name={} key={} → DB 조회 예정", store, getName(), key);
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = delegate.get(key, type);
        if (value != null) {
            log.info("[CACHE HIT {}] name={} key={}", store, getName(), key);
        } else {
            log.info("[CACHE MISS {}] name={} key={}", store, getName(), key);
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper cached = delegate.get(key);
        if (cached != null) {
            log.info("[CACHE HIT {}] name={} key={}", store, getName(), key);
            @SuppressWarnings("unchecked")
            T value = (T) cached.get();
            return value;
        }
        log.info("[CACHE MISS {}] name={} key={} → loader", store, getName(), key);
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        log.info("[CACHE PUT {}] name={} key={}", store, getName(), key);
        delegate.put(key, value);
    }

    @Override
    public void evict(Object key) {
        log.info("[CACHE EVICT {}] name={} key={}", store, getName(), key);
        delegate.evict(key);
    }

    @Override
    public void clear() {
        log.info("[CACHE CLEAR {}] name={}", store, getName());
        delegate.clear();
    }
}
