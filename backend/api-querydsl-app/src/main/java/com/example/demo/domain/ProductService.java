package com.example.demo.domain;

import com.example.demo.web.BusinessException;
import com.example.demo.web.ProductRequest;
import com.example.demo.web.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductQueryRepository productQueryRepository;
    private final ProductRepository productRepository;

    public ProductService(ProductQueryRepository productQueryRepository, ProductRepository productRepository) {
        this.productQueryRepository = productQueryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list() {
        return productQueryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = productQueryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND", "product not found: " + id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        productRepository.save(product);
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }
}
