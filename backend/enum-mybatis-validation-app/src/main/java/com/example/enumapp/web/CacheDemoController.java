package com.example.enumapp.web;

import com.example.enumapp.domain.order.OrderService;
import com.example.enumapp.web.dto.OrderResponse;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 캐시 데모 안내 — 실제 캐시는 Service 계층({@link com.example.enumapp.domain.order.OrderQueryService})에 적용.
 */
@Tag(name = "Cache Demo", description = "Spring Cache (@Cacheable/@CachePut/@CacheEvict) 동작 확인")
@RestController
@RequestMapping("/api/demo/cache")
public class CacheDemoController {

    private final CacheManager cacheManager;
    private final OrderService orderService;

    public CacheDemoController(CacheManager cacheManager, OrderService orderService) {
        this.cacheManager = cacheManager;
        this.orderService = orderService;
    }

    @Operation(summary = "캐시 적용 위치 및 통계", description = """
            호출 흐름:
            Controller(OrderController/OrderResourceController)
            → OrderService (쓰기 시 @CachePut/@CacheEvict)
            → OrderQueryService (@Cacheable — 여기서 캐시 hit/miss)
            → OrderMapper → MyBatisSqlLoggingInterceptor ([MYBATIS→DB] 로그)

            같은 GET /api/orders/{id} 를 두 번 호출하면 두 번째는 [CACHE→DB]/[MYBATIS→DB] 로그가 없어야 한다.
            """)
    @GetMapping
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("flow", List.of(
                "OrderController / OrderResourceController",
                "OrderService",
                "OrderQueryService (@Cacheable)",
                "OrderMapper (MyBatis XML)",
                "DB"
        ));
        body.put("caches", List.of(
                Map.of("name", "orders", "key", "#id", "usedBy", "GET /api/orders/{id}"),
                Map.of("name", "orders-list", "key", "'all'", "usedBy", "POST /api/orders/search {} / POST /api/resource/orders"),
                Map.of("name", "orders-search", "key", "#criteria.cacheKey()", "usedBy", "POST /api/orders/search (조건 있을 때)")
        ));
        body.put("writeEvict", List.of(
                "create / update / cancel → @CachePut(orders) + list/search evict",
                "delete / deleteMany → @CacheEvict"
        ));
        body.put("examples", List.of(
                Map.of(
                        "annotation", "@Cacheable",
                        "method", "GET /api/orders/{id}",
                        "effect", "hit 이면 메서드/DB 생략"
                ),
                Map.of(
                        "annotation", "@CachePut",
                        "method", "PUT /api/demo/cache/orders/{id}?customerName=...",
                        "effect", "항상 DB 갱신 + 반환값으로 orders 캐시 덮어씀"
                ),
                Map.of(
                        "annotation", "@CacheEvict",
                        "method", "DELETE /api/demo/cache/orders/{id}",
                        "effect", "orders 키만 삭제. DB 행은 유지. 다음 GET 은 miss"
                )
        ));
        body.put("stats", cacheStats());
        return body;
    }

    @Operation(summary = "@CachePut 예제", description = """
            이름을 바꾸고 반환값으로 orders 캐시를 덮어쓴다.
            이후 GET /api/orders/{id} 는 DB 없이 새 이름을 반환한다.
            """)
    @PutMapping("/orders/{id}")
    public OrderResponse cachePut(
            @PathVariable Long id,
            @RequestParam String customerName
    ) {
        return orderService.cachePutRename(id, customerName);
    }

    @Operation(summary = "@CacheEvict 예제", description = """
            orders 캐시에서 해당 id 만 지운다. DB 는 삭제하지 않는다.
            이후 GET /api/orders/{id} 는 다시 [CACHE→DB]/[MYBATIS→DB] 로그가 찍힌다.
            """)
    @DeleteMapping("/orders/{id}")
    public Map<String, Object> cacheEvict(@PathVariable Long id) {
        orderService.cacheEvictById(id);
        return Map.of("evicted", true, "id", id, "cache", "orders");
    }

    private Map<String, Object> cacheStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        for (String name : List.of("orders", "orders-list", "orders-search")) {
            var cache = cacheManager.getCache(name);
            if (cache instanceof CaffeineCache caffeineCache) {
                CacheStats s = caffeineCache.getNativeCache().stats();
                stats.put(name, Map.of(
                        "hitCount", s.hitCount(),
                        "missCount", s.missCount(),
                        "size", caffeineCache.getNativeCache().estimatedSize()
                ));
            }
        }
        return stats;
    }
}
