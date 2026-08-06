package com.example.enumapp.domain.order

import com.example.enumapp.common.code.CodeEnum

enum class OrderStatus(
    override val code: String,
    override val description: String
) : CodeEnum {
    PENDING("P", "대기"),
    PAID("A", "결제완료"),
    CANCELLED("C", "취소")
}
