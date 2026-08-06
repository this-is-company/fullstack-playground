from __future__ import annotations

from collections.abc import Collection
from typing import Any

from sqlalchemy import and_, ColumnElement
from sqlalchemy.sql.elements import ColumnElement as CE


class ConditionBuilder:
    """
    SQLAlchemy where(...) 에 바로 넣을 수 있는 null-safe 조건 팩토리.
    값이 없으면 None 을 반환하며, and_combine 이 None 을 걸러낸다.
    """

    _INSTANCE: ConditionBuilder | None = None

    @classmethod
    def create(cls) -> ConditionBuilder:
        if cls._INSTANCE is None:
            cls._INSTANCE = ConditionBuilder()
        return cls._INSTANCE

    @staticmethod
    def _has_text(value: str | None) -> bool:
        return value is not None and bool(str(value).strip())

    def eq(self, path: Any, value: Any | None) -> ColumnElement[bool] | None:
        if value is None:
            return None
        return path == value

    def contains_ignore_case(self, path: Any, value: str | None) -> ColumnElement[bool] | None:
        """부분 일치 (대소문자 무시)"""
        if not self._has_text(value):
            return None
        assert value is not None
        return path.ilike(f"%{value.strip()}%")

    def like(self, path: Any, value: str | None) -> ColumnElement[bool] | None:
        """LIKE %value%"""
        if not self._has_text(value):
            return None
        assert value is not None
        return path.like(f"%{value.strip()}%")

    def between(
        self, path: Any, from_: Any | None, to: Any | None
    ) -> ColumnElement[bool] | None:
        """between. 한쪽만 있으면 goe/loe"""
        if from_ is not None and to is not None:
            return path.between(from_, to)
        if from_ is not None:
            return path >= from_
        if to is not None:
            return path <= to
        return None

    def range(
        self,
        path: Any,
        from_: Any | None,
        to: Any | None,
        to_inclusive: bool,
    ) -> ColumnElement[bool] | None:
        """to_inclusive=False 이면 to 는 exclusive (<)"""
        parts: list[CE[bool]] = []
        if from_ is not None:
            parts.append(path >= from_)
        if to is not None:
            parts.append(path <= to if to_inclusive else path < to)
        if not parts:
            return None
        return and_(*parts) if len(parts) > 1 else parts[0]

    def in_(self, path: Any, values: Collection[Any] | None) -> ColumnElement[bool] | None:
        if values is None or len(values) == 0:
            return None
        return path.in_(list(values))

    def goe(self, path: Any, value: Any | None) -> ColumnElement[bool] | None:
        if value is None:
            return None
        return path >= value

    def loe(self, path: Any, value: Any | None) -> ColumnElement[bool] | None:
        if value is None:
            return None
        return path <= value

    @staticmethod
    def and_combine(*predicates: ColumnElement[bool] | None) -> ColumnElement[bool] | None:
        parts = [p for p in predicates if p is not None]
        if not parts:
            return None
        if len(parts) == 1:
            return parts[0]
        return and_(*parts)
