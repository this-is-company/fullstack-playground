package com.example.cacheapp;

import com.example.cacheapp.config.TwoLevelCache;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import static org.assertj.core.api.Assertions.assertThat;

class TwoLevelCacheTest {

    @Test
    void missThenL1ThenL2AfterL1Evict() {
        ConcurrentMapCache l1 = new ConcurrentMapCache("products");
        ConcurrentMapCache l2 = new ConcurrentMapCache("products");
        TwoLevelCache cache = new TwoLevelCache("products", l1, l2);

        assertThat(cache.get(1L)).isNull();

        cache.put(1L, "keyboard");
        assertThat(cache.get(1L).get()).isEqualTo("keyboard");
        assertThat(l1.get(1L).get()).isEqualTo("keyboard");
        assertThat(l2.get(1L).get()).isEqualTo("keyboard");

        cache.evictL1Only(1L);
        assertThat(l1.get(1L)).isNull();
        assertThat(cache.get(1L).get()).isEqualTo("keyboard");
        assertThat(l1.get(1L).get()).isEqualTo("keyboard");
    }
}
