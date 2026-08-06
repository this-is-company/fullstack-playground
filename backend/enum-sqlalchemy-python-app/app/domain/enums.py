from __future__ import annotations

from enum import Enum

from sqlalchemy import String, TypeDecorator

from app.common.code_enum import CodeEnums


class OrderStatus(Enum):
    PENDING = ("P", "대기")
    PAID = ("A", "결제완료")
    CANCELLED = ("C", "취소")

    def __init__(self, code: str, description: str) -> None:
        self._code = code
        self._description = description

    @property
    def code(self) -> str:
        return self._code

    @property
    def description(self) -> str:
        return self._description


class PayMethod(Enum):
    CARD = ("CARD", "카드")
    CASH = ("CASH", "현금")
    TRANSFER = ("TRANSFER", "계좌이체")

    def __init__(self, code: str, description: str) -> None:
        self._code = code
        self._description = description

    @property
    def code(self) -> str:
        return self._code

    @property
    def description(self) -> str:
        return self._description


class UserGrade(Enum):
    BASIC = ("B", "기본")
    SILVER = ("S", "실버")
    GOLD = ("G", "골드")

    def __init__(self, code: str, description: str) -> None:
        self._code = code
        self._description = description

    @property
    def code(self) -> str:
        return self._code

    @property
    def description(self) -> str:
        return self._description


class CodeEnumType(TypeDecorator):
    """CodeEnum → DB code 문자열 공통 TypeDecorator."""

    impl = String
    cache_ok = True

    def __init__(self, enum_class: type[Enum], length: int = 20) -> None:
        super().__init__(length)
        self.enum_class = enum_class

    def process_bind_param(self, value, dialect):  # noqa: ARG002
        return CodeEnums.to_code(value)

    def process_result_value(self, value, dialect):  # noqa: ARG002
        return CodeEnums.from_code(self.enum_class, value)
