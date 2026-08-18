package com.example.demo.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "응답 구분. SUCCESS / BUSINESS_ERROR / SERVER_ERROR")
public enum ApiStatus {
    SUCCESS,
    BUSINESS_ERROR,
    SERVER_ERROR
}
