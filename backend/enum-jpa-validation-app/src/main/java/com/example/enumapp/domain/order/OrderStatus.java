package com.example.enumapp.domain.order;

import com.example.enumapp.common.code.CodeEnum;

public enum OrderStatus implements CodeEnum {
    PENDING("P", "대기"),
    PAID("A", "결제완료"),
    CANCELLED("C", "취소");

    private final String code;
    private final String description;

    OrderStatus(String code, String description) {
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
