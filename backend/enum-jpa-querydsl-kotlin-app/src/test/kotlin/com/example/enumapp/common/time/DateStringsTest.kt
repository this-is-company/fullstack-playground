package com.example.enumapp.common.time

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DateStringsTest {

    @Test
    fun convertsBlankToNull() {
        assertThat(DateStrings.toLocalDate(null)).isNull()
        assertThat(DateStrings.toLocalDate("")).isNull()
        assertThat(DateStrings.toLocalDate("  ")).isNull()
        assertThat(DateStrings.toLocalDateTime(null)).isNull()
        assertThat(DateStrings.toLocalDateTime("")).isNull()
    }

    @Test
    fun parsesValidValues() {
        assertThat(DateStrings.toLocalDate("2026-08-06")).isEqualTo(LocalDate.of(2026, 8, 6))
        assertThat(DateStrings.toLocalDateTime("2026-08-06T10:15:30"))
            .isEqualTo(LocalDateTime.of(2026, 8, 6, 10, 15, 30))
    }

    @Test
    fun throwsOnInvalidFormat() {
        assertThatThrownBy { DateStrings.toLocalDate("06/08/2026") }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { DateStrings.toLocalDateTime("2026-08-06 10:15:30") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
