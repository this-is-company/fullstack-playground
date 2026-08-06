from __future__ import annotations


class ApiMessageCodes:
    VALIDATION_ERROR = "VALIDATION_ERROR"
    BAD_REQUEST = "BAD_REQUEST"
    ORDER_NOT_FOUND = "ORDER_NOT_FOUND"
    FILE_NOT_FOUND = "FILE_NOT_FOUND"
    INTERNAL_ERROR = "INTERNAL_ERROR"


class BusinessException(Exception):
    """클라이언트/도메인 규칙 위반(4xx). 서버 장애(5xx)와 구분한다."""

    def __init__(self, status: int, message_code: str, message: str) -> None:
        if not (400 <= status < 500):
            raise ValueError("BusinessException status must be 4xx")
        self.status = status
        self.message_code = message_code
        super().__init__(message)

    @classmethod
    def bad_request(cls, message_code: str, message: str) -> BusinessException:
        return cls(400, message_code, message)

    @classmethod
    def not_found(cls, message_code: str, message: str) -> BusinessException:
        return cls(404, message_code, message)
