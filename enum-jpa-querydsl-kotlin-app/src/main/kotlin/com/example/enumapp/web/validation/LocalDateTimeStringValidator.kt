package com.example.enumapp.web.validation

import com.example.enumapp.common.time.DateFormats
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.regex.Pattern

class LocalDateTimeStringValidator : ConstraintValidator<LocalDateTimeString, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null || value.isBlank()) {
            return true
        }
        val trimmed = value.trim()
        if (!FORMAT.matcher(trimmed).matches()) {
            return false
        }
        return try {
            LocalDateTime.parse(trimmed, PARSER)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }

    companion object {
        private val FORMAT: Pattern = Pattern.compile("^" + DateFormats.LOCAL_DATE_TIME + "$")
        private val PARSER: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT)
    }
}
