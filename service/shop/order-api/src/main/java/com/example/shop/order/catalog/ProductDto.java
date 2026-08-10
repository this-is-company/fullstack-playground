package com.example.shop.order.catalog;

import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        boolean active
) {
}
