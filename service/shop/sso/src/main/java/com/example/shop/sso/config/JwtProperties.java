package com.example.shop.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shop.jwt")
public record JwtProperties(String secret, long expiresInSeconds) {
}
