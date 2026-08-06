package com.example.enumapp.common.code

import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.domain.order.UserGrade
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CodeEnumsTest {

    @Test
    fun fromCode_returnsNull_whenCodeIsNullOrBlank() {
        assertThat(CodeEnums.fromCode(OrderStatus::class.java, null)).isNull()
        assertThat(CodeEnums.fromCode(OrderStatus::class.java, "")).isNull()
        assertThat(CodeEnums.fromCode(OrderStatus::class.java, "   ")).isNull()
        assertThat(CodeEnums.fromCode(PayMethod::class.java, null)).isNull()
        assertThat(CodeEnums.fromCode(UserGrade::class.java, "")).isNull()
    }

    @Test
    fun fromCode_mapsKnownCodes() {
        assertThat(CodeEnums.fromCode(OrderStatus::class.java, "P")).isEqualTo(OrderStatus.PENDING)
        assertThat(CodeEnums.fromCode(PayMethod::class.java, "CARD")).isEqualTo(PayMethod.CARD)
        assertThat(CodeEnums.fromCode(UserGrade::class.java, "G")).isEqualTo(UserGrade.GOLD)
    }

    @Test
    fun fromCode_throws_whenUnknown() {
        assertThatThrownBy { CodeEnums.fromCode(OrderStatus::class.java, "XX") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Unknown code")
    }

    @Test
    fun toCode_returnsNull_whenEnumNull() {
        assertThat(CodeEnums.toCode(null)).isNull()
        assertThat(CodeEnums.toCode(OrderStatus.PAID)).isEqualTo("A")
    }
}
