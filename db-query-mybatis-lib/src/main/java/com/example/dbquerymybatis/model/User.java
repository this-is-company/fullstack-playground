package com.example.dbquerymybatis.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String name,
        String email,
        String department,
        LocalDateTime createdAt
) {
}
