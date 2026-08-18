package com.example.cacheapp.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Cacheable(cacheNames = "products", key = "#id")
    public Product get(Long id) {
        log.info("[CACHE MISS] products key={} → MyBatis/DB", id);
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found: " + id);
        }
        return product;
    }

    @Cacheable(cacheNames = "products-list", key = "'all'")
    public List<Product> list() {
        log.info("[CACHE MISS] products-list → MyBatis/DB");
        return productMapper.findAll();
    }

    @Caching(
            put = @CachePut(cacheNames = "products", key = "#id"),
            evict = @CacheEvict(cacheNames = "products-list", allEntries = true)
    )
    public Product updateStock(Long id, int stock) {
        log.info("[CACHE PUT] products key={} stock={}", id, stock);
        int updated = productMapper.updateStock(id, stock);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found: " + id);
        }
        return productMapper.findById(id);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "products", key = "#id"),
            @CacheEvict(cacheNames = "products-list", allEntries = true)
    })
    public void evict(Long id) {
        log.info("[CACHE EVICT] products key={}", id);
    }
}
