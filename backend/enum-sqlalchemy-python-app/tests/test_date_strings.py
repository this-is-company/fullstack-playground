from __future__ import annotations

from datetime import date, datetime

import pytest

from app.common.date_strings import DateStrings


class TestDateStrings:
    def test_converts_blank_to_null(self) -> None:
        assert DateStrings.to_local_date(None) is None
        assert DateStrings.to_local_date("") is None
        assert DateStrings.to_local_date("  ") is None
        assert DateStrings.to_local_datetime(None) is None
        assert DateStrings.to_local_datetime("") is None

    def test_parses_valid_values(self) -> None:
        assert DateStrings.to_local_date("2026-08-06") == date(2026, 8, 6)
        assert DateStrings.to_local_datetime("2026-08-06T10:15:30") == datetime(
            2026, 8, 6, 10, 15, 30
        )

    def test_throws_on_invalid_format(self) -> None:
        with pytest.raises(ValueError):
            DateStrings.to_local_date("06/08/2026")
        with pytest.raises(ValueError):
            DateStrings.to_local_datetime("2026-08-06 10:15:30")
