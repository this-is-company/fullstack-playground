package com.example.enumapp.web.validation

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class FieldConstraintUnitTest {

    private val dateValidator = LocalDateStringValidator()
    private val dateTimeValidator = LocalDateTimeStringValidator()
    private val noSpecialCharsValidator = NoSpecialCharsValidator()
    private val unusedContext: ConstraintValidatorContext = mock(ConstraintValidatorContext::class.java)

    @Test
    fun localDate_allowsBlankAndValid() {
        assertThat(dateValidator.isValid(null, unusedContext)).isTrue()
        assertThat(dateValidator.isValid("  ", unusedContext)).isTrue()
        assertThat(dateValidator.isValid("2026-08-06", unusedContext)).isTrue()
        assertThat(dateValidator.isValid("2026-13-01", unusedContext)).isFalse()
        assertThat(dateValidator.isValid("2026/08/06", unusedContext)).isFalse()
    }

    @Test
    fun localDateTime_requiresSecondsAndT() {
        assertThat(dateTimeValidator.isValid("2026-08-06T09:00:00", unusedContext)).isTrue()
        assertThat(dateTimeValidator.isValid("2026-08-06T09:00", unusedContext)).isFalse()
        assertThat(dateTimeValidator.isValid("2026-08-06 09:00:00", unusedContext)).isFalse()
    }

    @Test
    fun noSpecialChars_allowsHangulAndRejectsSymbols() {
        assertThat(noSpecialCharsValidator.isValid("홍길동", unusedContext)).isTrue()
        assertThat(noSpecialCharsValidator.isValid("SKU-1_A.B", unusedContext)).isTrue()
        assertThat(noSpecialCharsValidator.isValid("a<b>", unusedContext)).isFalse()
        assertThat(noSpecialCharsValidator.isValid("a@b", unusedContext)).isFalse()
    }
}