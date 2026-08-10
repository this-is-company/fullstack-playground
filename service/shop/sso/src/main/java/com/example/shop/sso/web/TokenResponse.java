package com.example.shop.sso.web;

import java.util.List;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        List<String> roles
) {
}
