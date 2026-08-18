package com.example.cacheapp.web;

import com.example.cacheapp.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Cache Demo")
@RestController
@RequestMapping("/api/demo/cache")
public class CacheDemoController {

    private final CacheManager cacheManager;
    private final ProductService productService;

    public CacheDemoController(CacheManager cacheManager, ProductService productService) {
        this.cacheManager = cacheManager;
        this.productService = productService;
    }

    @GetMapping
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("store", "redis");
        body.put("cacheManager", cacheManager.getClass().getSimpleName());
        body.put("howToTell", List.of(
                "[CACHE MISS] + [MYBATIS→DB] → DB에서 읽음",
                "둘 다 없으면 → Redis hit",
                "redis-cli: KEYS *   GET products::1"
        ));
        return body;
    }

    @Operation(summary = "@CacheEvict — Redis 키 삭제. 다음 GET 은 다시 SQL")
    @DeleteMapping("/products/{id}")
    public Map<String, Object> evict(@PathVariable Long id) {
        productService.evict(id);
        return Map.of("evicted", true, "id", id);
    }
}
