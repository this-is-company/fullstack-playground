package com.example.enumapp.common.code;

import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.domain.order.UserGrade;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodeEnumsTest {

    @Test
    void fromCode_returnsNull_whenCodeIsNullOrBlank() {
        assertThat(CodeEnums.fromCode(OrderStatus.class, null)).isNull();
        assertThat(CodeEnums.fromCode(OrderStatus.class, "")).isNull();
        assertThat(CodeEnums.fromCode(OrderStatus.class, "   ")).isNull();
        assertThat(CodeEnums.fromCode(PayMethod.class, null)).isNull();
        assertThat(CodeEnums.fromCode(UserGrade.class, "")).isNull();
    }

    @Test
    void fromCode_mapsKnownCodes() {
        assertThat(CodeEnums.fromCode(OrderStatus.class, "P")).isEqualTo(OrderStatus.PENDING);
        assertThat(CodeEnums.fromCode(PayMethod.class, "CARD")).isEqualTo(PayMethod.CARD);
        assertThat(CodeEnums.fromCode(UserGrade.class, "G")).isEqualTo(UserGrade.GOLD);
    }

    @Test
    void fromCode_throws_whenUnknown() {
        assertThatThrownBy(() -> CodeEnums.fromCode(OrderStatus.class, "XX"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown code");
    }

    @Test
    void toCode_returnsNull_whenEnumNull() {
        assertThat(CodeEnums.toCode(null)).isNull();
        assertThat(CodeEnums.toCode(OrderStatus.PAID)).isEqualTo("A");
    }
}
