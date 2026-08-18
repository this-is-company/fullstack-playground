package com.example.demo.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품")
public class ProductResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Keyboard")
    private String name;

    @Schema(example = "49000")
    private int price;

    @Schema(example = "10")
    private int stock;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, int price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
