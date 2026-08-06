from __future__ import annotations

from typing import Protocol, TypeVar, runtime_checkable


@runtime_checkable
class CodeEnum(Protocol):
    """DB/API 코드값을 가지는 Enum 추상화."""

    @property
    def code(self) -> str: ...

    @property
    def description(self) -> str: ...


E = TypeVar("E", bound=CodeEnum)


class CodeEnums:
    @staticmethod
    def from_code(enum_type: type[E], code: str | None) -> E | None:
        if code is None or not str(code).strip():
            return None
        for member in enum_type:  # type: ignore[attr-defined]
            if member.code == code:
                return member
        raise ValueError(f"Unknown code '{code}' for enum {enum_type.__name__}")

    @staticmethod
    def to_code(value: CodeEnum | None) -> str | None:
        return None if value is None else value.code
