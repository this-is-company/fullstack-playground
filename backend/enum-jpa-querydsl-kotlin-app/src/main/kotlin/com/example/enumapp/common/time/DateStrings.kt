package com.example.enumapp.common.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Request 문자열 날짜를 LocalDate / LocalDateTime 으로 변환한다.
 * null / blank 는 null 로 변환한다.
 * 형식은 [DateFormats] 와 동일하게 엄격히 맞춘다.
 */
object DateStrings {

    val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
        .withResolverStyle(ResolverStyle.STRICT)

    val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
        .withResolverStyle(ResolverStyle.STRICT)

    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDate.parse(value.trim(), DATE)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(
                "Invalid LocalDate format (expected ${DateFormats.LOCAL_DATE_PATTERN}): $value",
                e
            )
        }
    }

    @JvmStatic
    fun toLocalDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDateTime.parse(value.trim(), DATE_TIME)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(
                "Invalid LocalDateTime format (expected ${DateFormats.LOCAL_DATE_TIME_PATTERN}): $value",
                e
            )
        }
    }
}
