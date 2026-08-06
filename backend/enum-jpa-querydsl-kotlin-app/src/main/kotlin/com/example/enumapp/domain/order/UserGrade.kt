package com.example.enumapp.domain.order

import com.example.enumapp.common.code.CodeEnum

enum class UserGrade(
    override val code: String,
    override val description: String
) : CodeEnum {
    BASIC("B", "기본"),
    SILVER("S", "실버"),
    GOLD("G", "골드")
}
