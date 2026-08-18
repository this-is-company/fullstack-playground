package com.example.demo.domain;

import com.example.demo.web.BusinessException;
import com.example.demo.web.ProductRequest;
import com.example.demo.web.ProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<ProductResponse> list() {
        return productMapper.findAll().stream().map(this::toResponse).toList();
    }

    public ProductResponse get(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw BusinessException.notFound("PRODUCT_NOT_FOUND", "product not found: " + id);
        }
        return toResponse(product);
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        productMapper.insert(product);
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}
