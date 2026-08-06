package com.example.enumapp.domain.order;

import com.example.enumapp.common.code.CodeEnum;

public enum PayMethod implements CodeEnum {
    CARD("CARD", "카드"),
    CASH("CASH", "현금"),
    TRANSFER("TRANSFER", "계좌이체");

    private final String code;
    private final String description;

    PayMethod(String code, String description) {
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
