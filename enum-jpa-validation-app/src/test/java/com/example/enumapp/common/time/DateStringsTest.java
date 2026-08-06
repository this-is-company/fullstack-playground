package com.example.enumapp.common.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateStringsTest {

    @Test
    void convertsBlankToNull() {
        assertThat(DateStrings.toLocalDate(null)).isNull();
        assertThat(DateStrings.toLocalDate("")).isNull();
        assertThat(DateStrings.toLocalDate("  ")).isNull();
        assertThat(DateStrings.toLocalDateTime(null)).isNull();
        assertThat(DateStrings.toLocalDateTime("")).isNull();
    }

    @Test
    void parsesValidValues() {
        assertThat(DateStrings.toLocalDate("2026-08-06")).isEqualTo(LocalDate.of(2026, 8, 6));
        assertThat(DateStrings.toLocalDateTime("2026-08-06T10:15:30"))
                .isEqualTo(LocalDateTime.of(2026, 8, 6, 10, 15, 30));
    }

    @Test
    void throwsOnInvalidFormat() {
        assertThatThrownBy(() -> DateStrings.toLocalDate("06/08/2026"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DateStrings.toLocalDateTime("2026-08-06 10:15:30"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
