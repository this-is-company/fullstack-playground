package com.example.enumapp.common.time

/**
 * Request 문자열 날짜 패턴 (LocalDate / LocalDateTime).
 */
object DateFormats {

    /** yyyy-MM-dd */
    const val LOCAL_DATE = "\\d{4}-\\d{2}-\\d{2}"

    /** yyyy-MM-dd'T'HH:mm:ss */
    const val LOCAL_DATE_TIME = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"

    const val LOCAL_DATE_PATTERN = "yyyy-MM-dd"
    const val LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
}
