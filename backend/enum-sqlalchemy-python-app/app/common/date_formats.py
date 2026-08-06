"""Request 문자열 날짜 패턴 (LocalDate / LocalDateTime)."""

# yyyy-MM-dd
LOCAL_DATE = r"\d{4}-\d{2}-\d{2}"

# yyyy-MM-dd'T'HH:mm:ss
LOCAL_DATE_TIME = r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}"

LOCAL_DATE_PATTERN = "yyyy-MM-dd"
LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
