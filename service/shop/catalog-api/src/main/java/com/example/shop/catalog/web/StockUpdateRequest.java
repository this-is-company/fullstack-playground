package com.example.shop.catalog.web;

public record StockUpdateRequest(
        Integer delta,
        Integer stock
) {
}
