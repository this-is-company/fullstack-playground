package com.example.demo.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 생성 요청")
public class ProductRequest {

    @NotBlank
    @Schema(description = "상품명", example = "Keyboard")
    private String name;

    @NotNull
    @Min(0)
    @Schema(description = "가격", example = "49000")
    private Integer price;

    @NotNull
    @Min(0)
    @Schema(description = "재고", example = "10")
    private Integer stock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
