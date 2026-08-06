package com.example.enumapp.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Request 문자열 날짜를 LocalDate / LocalDateTime 으로 변환한다.
 * null / blank 는 null 로 변환한다.
 * 형식은 {@link DateFormats} 와 동일하게 엄격히 맞춘다.
 */
public final class DateStrings {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private DateStrings() {
    }

    public static LocalDate toLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid LocalDate format (expected " + DateFormats.LOCAL_DATE_PATTERN + "): " + value, e
            );
        }
    }

    public static LocalDateTime toLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid LocalDateTime format (expected " + DateFormats.LOCAL_DATE_TIME_PATTERN + "): " + value, e
            );
        }
    }
}
