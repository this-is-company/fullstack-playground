package com.example.shop.sso.web;

import java.util.List;

public record UserInfoResponse(
        String username,
        List<String> roles
) {
}
