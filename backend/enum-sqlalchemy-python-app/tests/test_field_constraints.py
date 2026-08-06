from __future__ import annotations

import pytest

from app.web.validators import local_date_string, local_datetime_string, no_special_chars


class TestFieldConstraints:
    def test_local_date_allows_blank_and_valid(self) -> None:
        assert local_date_string(None) is None
        assert local_date_string("  ") == "  "
        assert local_date_string("2026-08-06") == "2026-08-06"
        with pytest.raises(ValueError):
            local_date_string("2026-13-01")
        with pytest.raises(ValueError):
            local_date_string("2026/08/06")

    def test_local_datetime_requires_seconds_and_t(self) -> None:
        assert local_datetime_string("2026-08-06T09:00:00") == "2026-08-06T09:00:00"
        with pytest.raises(ValueError):
            local_datetime_string("2026-08-06T09:00")
        with pytest.raises(ValueError):
            local_datetime_string("2026-08-06 09:00:00")

    def test_no_special_chars_allows_hangul_and_rejects_symbols(self) -> None:
        assert no_special_chars("홍길동") == "홍길동"
        assert no_special_chars("SKU-1_A.B") == "SKU-1_A.B"
        with pytest.raises(ValueError, match="special characters"):
            no_special_chars("a<b>")
        with pytest.raises(ValueError, match="special characters"):
            no_special_chars("a@b")
