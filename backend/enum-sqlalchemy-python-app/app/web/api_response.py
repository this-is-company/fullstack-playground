from __future__ import annotations

from typing import Any, Generic, TypeVar

from pydantic import BaseModel, ConfigDict, Field

T = TypeVar("T")


class ApiErrorBody(BaseModel):
    model_config = ConfigDict(exclude_none=True)

    message: str | None = None
    messageCode: str | None = None
    errors: dict[str, str] | None = None

    @classmethod
    def of(
        cls,
        message_code: str | None,
        message: str | None,
        errors: dict[str, str] | None = None,
    ) -> ApiErrorBody:
        return cls(message=message, messageCode=message_code, errors=errors)


class ApiResponse(BaseModel, Generic[T]):
    status: int
    error: bool
    result: Any = None

    @classmethod
    def ok(cls, status: int, result: Any = None) -> ApiResponse:
        return cls(status=status, error=False, result=result)

    @classmethod
    def fail(
        cls,
        status: int,
        message_code: str | None,
        message: str | None,
        errors: dict[str, str] | None = None,
    ) -> ApiResponse:
        return cls(
            status=status,
            error=True,
            result=ApiErrorBody.of(message_code, message, errors).model_dump(exclude_none=True),
        )
