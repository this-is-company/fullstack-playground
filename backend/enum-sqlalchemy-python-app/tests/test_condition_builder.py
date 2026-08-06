from __future__ import annotations

from sqlalchemy import Integer, String, column

from app.common.condition_builder import ConditionBuilder


NAME = column("name", String)
QTY = column("qty", Integer)
ID = column("id", Integer)


class TestConditionBuilder:
    def setup_method(self) -> None:
        self.condition = ConditionBuilder.create()

    def test_returns_null_when_value_missing(self) -> None:
        assert self.condition.eq(NAME, None) is None
        assert self.condition.like(NAME, "  ") is None
        assert self.condition.contains_ignore_case(NAME, None) is None
        assert self.condition.goe(QTY, None) is None
        assert self.condition.in_(ID, None) is None
        assert self.condition.in_(ID, []) is None
        assert self.condition.between(QTY, None, None) is None
        assert self.condition.range(QTY, None, None, True) is None

    def test_returns_predicate_when_value_present(self) -> None:
        assert self.condition.eq(NAME, "Kim") is not None
        assert self.condition.like(NAME, "Kim") is not None
        assert self.condition.contains_ignore_case(NAME, "kim") is not None
        assert self.condition.in_(ID, [1, 2]) is not None
        assert self.condition.goe(QTY, 5) is not None

    def test_between_and_range(self) -> None:
        both = self.condition.between(QTY, 1, 10)
        assert both is not None
        assert "between" in str(both).lower()

        assert self.condition.between(QTY, 3, None) is not None
        assert self.condition.between(QTY, None, 9) is not None

        inclusive = self.condition.range(QTY, 1, 10, True)
        exclusive = self.condition.range(QTY, 1, 10, False)
        assert inclusive is not None
        assert exclusive is not None
        assert str(inclusive) != str(exclusive)
