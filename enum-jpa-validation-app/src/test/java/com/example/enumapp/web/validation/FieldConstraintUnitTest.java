package com.example.enumapp.web.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldConstraintUnitTest {

    private final LocalDateStringValidator dateValidator = new LocalDateStringValidator();
    private final LocalDateTimeStringValidator dateTimeValidator = new LocalDateTimeStringValidator();
    private final NoSpecialCharsValidator noSpecialCharsValidator = new NoSpecialCharsValidator();

    @Test
    void localDate_allowsBlankAndValid() {
        assertThat(dateValidator.isValid(null, null)).isTrue();
        assertThat(dateValidator.isValid("  ", null)).isTrue();
        assertThat(dateValidator.isValid("2026-08-06", null)).isTrue();
        assertThat(dateValidator.isValid("2026-13-01", null)).isFalse();
        assertThat(dateValidator.isValid("2026/08/06", null)).isFalse();
    }

    @Test
    void localDateTime_requiresSecondsAndT() {
        assertThat(dateTimeValidator.isValid("2026-08-06T09:00:00", null)).isTrue();
        assertThat(dateTimeValidator.isValid("2026-08-06T09:00", null)).isFalse();
        assertThat(dateTimeValidator.isValid("2026-08-06 09:00:00", null)).isFalse();
    }

    @Test
    void noSpecialChars_allowsHangulAndRejectsSymbols() {
        assertThat(noSpecialCharsValidator.isValid("홍길동", null)).isTrue();
        assertThat(noSpecialCharsValidator.isValid("SKU-1_A.B", null)).isTrue();
        assertThat(noSpecialCharsValidator.isValid("a<b>", null)).isFalse();
        assertThat(noSpecialCharsValidator.isValid("a@b", null)).isFalse();
    }
}
