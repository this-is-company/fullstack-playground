package com.example.demo.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오류일 때만 채워진다. 성공이면 null")
public class ApiError {

    @Schema(description = "오류 코드", example = "PRODUCT_NOT_FOUND")
    private String code;

    @Schema(description = "오류 메시지", example = "product not found: 99")
    private String message;

    public ApiError() {
    }

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
