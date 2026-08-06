from __future__ import annotations

import pytest

from app.common.code_enum import CodeEnums
from app.domain.enums import OrderStatus, PayMethod, UserGrade


class TestCodeEnums:
    def test_from_code_returns_null_when_code_is_null_or_blank(self) -> None:
        assert CodeEnums.from_code(OrderStatus, None) is None
        assert CodeEnums.from_code(OrderStatus, "") is None
        assert CodeEnums.from_code(OrderStatus, "   ") is None
        assert CodeEnums.from_code(PayMethod, None) is None
        assert CodeEnums.from_code(UserGrade, "") is None

    def test_from_code_maps_known_codes(self) -> None:
        assert CodeEnums.from_code(OrderStatus, "P") is OrderStatus.PENDING
        assert CodeEnums.from_code(PayMethod, "CARD") is PayMethod.CARD
        assert CodeEnums.from_code(UserGrade, "G") is UserGrade.GOLD

    def test_from_code_throws_when_unknown(self) -> None:
        with pytest.raises(ValueError, match="Unknown code"):
            CodeEnums.from_code(OrderStatus, "XX")

    def test_to_code_returns_null_when_enum_null(self) -> None:
        assert CodeEnums.to_code(None) is None
        assert CodeEnums.to_code(OrderStatus.PAID) == "A"
