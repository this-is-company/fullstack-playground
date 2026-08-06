package com.example.enumapp.common.time;

/**
 * Request 문자열 날짜 패턴 (LocalDate / LocalDateTime).
 */
public final class DateFormats {

    /** yyyy-MM-dd */
    public static final String LOCAL_DATE = "\\d{4}-\\d{2}-\\d{2}";

    /** yyyy-MM-dd'T'HH:mm:ss */
    public static final String LOCAL_DATE_TIME = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}";

    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private DateFormats() {
    }
}
