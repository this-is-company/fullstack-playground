package com.example.cacheapp.web;

import com.example.cacheapp.config.TwoLevelCache;
import com.example.cacheapp.config.TwoLevelCacheManager;
import com.example.cacheapp.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Cache Demo", description = "L1 Caffeine + L2 Redis. L1 만 지우면 다음은 L2 hit (SQL 없음)")
@RestController
@RequestMapping("/api/demo/cache")
public class CacheDemoController {

    private final CacheManager cacheManager;
    private final ProductService productService;
    private final String cacheMode;

    public CacheDemoController(
            CacheManager cacheManager,
            ProductService productService,
            @Value("${app.cache.mode:both}") String cacheMode
    ) {
        this.cacheManager = cacheManager;
        this.productService = productService;
        this.cacheMode = cacheMode;
    }

    @GetMapping
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", cacheMode);
        body.put("store", switch (cacheMode) {
            case "caffeine" -> "Caffeine only";
            case "redis" -> "Redis only";
            default -> "caffeine(L1) + redis(L2)";
        });
        body.put("cacheManager", cacheManager.getClass().getSimpleName());
        body.put("howToTell", List.of(
                "[CACHE MISS L1+L2] + [MYBATIS→DB] → DB",
                "[CACHE HIT L1-Caffeine] → SQL 없음",
                "[CACHE HIT L2-Redis] → SQL 없음, L1 다시 채움",
                "DELETE /api/demo/cache/l1/products/{id} 후 GET → L2 hit"
        ));
        return body;
    }

    @Operation(summary = "L1+L2 모두 evict — 다음 GET 은 DB")
    @DeleteMapping("/products/{id}")
    public Map<String, Object> evictBoth(@PathVariable Long id) {
        productService.evict(id);
        return Map.of("evicted", "L1+L2", "id", id);
    }

    @Operation(summary = "L1(Caffeine)만 evict — 다음 GET 은 L2 Redis (SQL 없음)")
    @DeleteMapping("/l1/products/{id}")
    public Map<String, Object> evictL1(@PathVariable Long id) {
        if (cacheManager instanceof TwoLevelCacheManager twoLevel
                && twoLevel.getCache("products") instanceof TwoLevelCache cache) {
            cache.evictL1Only(id);
            return Map.of("evicted", "L1-only", "id", id);
        }
        return Map.of("evicted", false);
    }
}
