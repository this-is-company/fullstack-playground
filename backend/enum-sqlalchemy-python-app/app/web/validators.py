from __future__ import annotations

import re
from datetime import date, datetime

from app.common import date_formats

# Kotlin: ^[\p{L}\p{N}\s._-]*$  — letter/digit/space/._-
_NO_SPECIAL_CHARS = re.compile(r"^[\w\s.\-]*$", re.UNICODE)
_LOCAL_DATE = re.compile("^" + date_formats.LOCAL_DATE + "$")
_LOCAL_DATE_TIME = re.compile("^" + date_formats.LOCAL_DATE_TIME + "$")
_PROMO = re.compile(r"^[A-Za-z0-9_-]+$")


def no_special_chars(value: str | None) -> str | None:
    """null/blank 통과. 문자·숫자·공백·. _ - 만 허용."""
    if value is None or not str(value).strip():
        return value
    if not _NO_SPECIAL_CHARS.fullmatch(value):
        raise ValueError("must not contain special characters")
    return value


def local_date_string(value: str | None) -> str | None:
    """null/blank 통과. 값이 있으면 yyyy-MM-dd 이고 실제 date 로 파싱 가능해야 함."""
    if value is None or not str(value).strip():
        return value
    trimmed = value.strip()
    if not _LOCAL_DATE.fullmatch(trimmed):
        raise ValueError("must match LocalDate pattern yyyy-MM-dd")
    try:
        date.fromisoformat(trimmed)
    except ValueError as e:
        raise ValueError("must match LocalDate pattern yyyy-MM-dd") from e
    return value


def local_datetime_string(value: str | None) -> str | None:
    """null/blank 통과. 값이 있으면 yyyy-MM-dd'T'HH:mm:ss 이고 실제 datetime 파싱 가능."""
    if value is None or not str(value).strip():
        return value
    trimmed = value.strip()
    if not _LOCAL_DATE_TIME.fullmatch(trimmed):
        raise ValueError("must match LocalDateTime pattern yyyy-MM-dd'T'HH:mm:ss")
    try:
        datetime.strptime(trimmed, "%Y-%m-%dT%H:%M:%S")
    except ValueError as e:
        raise ValueError("must match LocalDateTime pattern yyyy-MM-dd'T'HH:mm:ss") from e
    return value


def promo_code_pattern(value: str | None) -> str | None:
    """null 이면 검사하지 않음. 값이 있으면 영문/숫자/_/- 만 허용."""
    if value is None:
        return value
    if not _PROMO.fullmatch(value):
        raise ValueError("promoCode must match pattern [A-Za-z0-9_-]+")
    return value
