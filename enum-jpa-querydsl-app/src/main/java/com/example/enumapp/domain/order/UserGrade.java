package com.example.enumapp.domain.order;

import com.example.enumapp.common.code.CodeEnum;

public enum UserGrade implements CodeEnum {
    BASIC("B", "기본"),
    SILVER("S", "실버"),
    GOLD("G", "골드");

    private final String code;
    private final String description;

    UserGrade(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
