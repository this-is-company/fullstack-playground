package com.example.shop.sso.user;

import java.util.List;

public record DemoUser(String username, String passwordHash, List<String> roles) {
}
