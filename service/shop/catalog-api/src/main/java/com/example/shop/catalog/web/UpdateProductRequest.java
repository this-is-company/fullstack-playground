package com.example.shop.catalog.web;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Boolean active
) {
}
