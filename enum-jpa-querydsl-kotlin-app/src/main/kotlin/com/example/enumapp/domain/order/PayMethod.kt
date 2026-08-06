package com.example.enumapp.domain.order

import com.example.enumapp.common.code.CodeEnum

enum class PayMethod(
    override val code: String,
    override val description: String
) : CodeEnum {
    CARD("CARD", "카드"),
    CASH("CASH", "현금"),
    TRANSFER("TRANSFER", "계좌이체")
}
