package com.example.cacheapp.web;

import com.example.cacheapp.product.Product;
import com.example.cacheapp.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Product", description = "Redis 캐시 + MyBatis. [CACHE MISS] vs [MYBATIS→DB]")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "목록 (@Cacheable products-list)")
    @GetMapping
    public List<Product> list() {
        return productService.list();
    }

    @Operation(summary = "단건 (@Cacheable products #id) — 두 번째 호출은 SQL 없음")
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return productService.get(id);
    }

    @Operation(summary = "재고 변경 (@CachePut)")
    @PatchMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id, @RequestParam int stock) {
        return productService.updateStock(id, stock);
    }
}
