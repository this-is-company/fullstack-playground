from __future__ import annotations

from datetime import date, datetime

from app.common import date_formats


class DateStrings:
    """
    Request 문자열 날짜를 date / datetime 으로 변환한다.
    null / blank 는 null 로 변환한다. 형식은 엄격히 맞춘다.
    """

    DATE_FMT = "%Y-%m-%d"
    DATE_TIME_FMT = "%Y-%m-%dT%H:%M:%S"

    @staticmethod
    def to_local_date(value: str | None) -> date | None:
        if value is None or not str(value).strip():
            return None
        trimmed = value.strip()
        try:
            return datetime.strptime(trimmed, DateStrings.DATE_FMT).date()
        except ValueError as e:
            raise ValueError(
                f"Invalid LocalDate format (expected {date_formats.LOCAL_DATE_PATTERN}): {value}"
            ) from e

    @staticmethod
    def to_local_datetime(value: str | None) -> datetime | None:
        if value is None or not str(value).strip():
            return None
        trimmed = value.strip()
        try:
            return datetime.strptime(trimmed, DateStrings.DATE_TIME_FMT)
        except ValueError as e:
            raise ValueError(
                f"Invalid LocalDateTime format (expected {date_formats.LOCAL_DATE_TIME_PATTERN}): {value}"
            ) from e
