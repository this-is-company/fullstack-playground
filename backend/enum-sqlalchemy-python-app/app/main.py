from __future__ import annotations

import logging
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import ValidationError
from sqlalchemy import text

from app.config import settings
from app.db import Base, engine
from app.web.api_response import ApiResponse
from app.web.exceptions import ApiMessageCodes, BusinessException
from app.web.file_router import router as file_router
from app.web.get_validation_router import router as get_validation_router
from app.web.order_router import router as order_router
from app.web.resource_router import router as resource_router
from app.web.schemas import ValidationFailed

logger = logging.getLogger(__name__)


def _init_schema() -> None:
    if settings.is_sqlite:
        import app.domain.models  # noqa: F401

        Base.metadata.create_all(bind=engine)
        return

    schema_path = Path(__file__).with_name("schema.sql")
    sql = schema_path.read_text(encoding="utf-8")
    with engine.begin() as conn:
        for stmt in sql.split(";"):
            chunk = stmt.strip()
            if chunk:
                conn.execute(text(chunk))


@asynccontextmanager
async def lifespan(_app: FastAPI):
    _init_schema()
    yield


app = FastAPI(
    title="enum-sqlalchemy-python-app",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)
app.include_router(order_router)
app.include_router(resource_router)
app.include_router(get_validation_router)
app.include_router(file_router)


def _field_errors_from_validation(exc: RequestValidationError | ValidationError) -> dict[str, str]:
    errors: dict[str, str] = {}
    for err in exc.errors():
        loc = err.get("loc", ())
        parts: list[str] = []
        for p in loc:
            if p in ("body", "query", "path", "header"):
                continue
            if isinstance(p, int):
                if parts:
                    parts[-1] = f"{parts[-1]}[{p}]"
                else:
                    parts.append(f"[{p}]")
            else:
                parts.append(str(p))
        key = ".".join(parts) if parts else "request"
        key = (
            key.replace("customer_name", "customerName")
            .replace("pay_method", "payMethod")
            .replace("user_grade", "userGrade")
            .replace("promo_code", "promoCode")
            .replace("product_name", "productName")
            .replace("order_date", "orderDate")
            .replace("from_date_time", "fromDateTime")
            .replace("to_date_time", "toDateTime")
            .replace("from_date", "fromDate")
            .replace("to_date", "toDate")
            .replace("min_quantity", "minQuantity")
        )
        msg = err.get("msg", "")
        if msg.startswith("Value error, "):
            msg = msg[len("Value error, ") :]
        errors[key] = msg
    return errors


@app.exception_handler(BusinessException)
async def handle_business(_request: Request, exc: BusinessException) -> JSONResponse:
    body = ApiResponse.fail(exc.status, exc.message_code, str(exc)).model_dump()
    return JSONResponse(status_code=exc.status, content=body)


@app.exception_handler(ValidationFailed)
async def handle_validation_failed(_request: Request, exc: ValidationFailed) -> JSONResponse:
    body = ApiResponse.fail(
        400, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", exc.errors
    ).model_dump()
    return JSONResponse(status_code=400, content=body)


@app.exception_handler(RequestValidationError)
async def handle_request_validation(
    _request: Request, exc: RequestValidationError
) -> JSONResponse:
    field_errors = _field_errors_from_validation(exc)
    body = ApiResponse.fail(
        400, ApiMessageCodes.VALIDATION_ERROR, "Validation failed", field_errors
    ).model_dump()
    return JSONResponse(status_code=400, content=body)


@app.exception_handler(ValueError)
async def handle_value_error(_request: Request, exc: ValueError) -> JSONResponse:
    body = ApiResponse.fail(400, ApiMessageCodes.BAD_REQUEST, str(exc)).model_dump()
    return JSONResponse(status_code=400, content=body)


@app.exception_handler(Exception)
async def handle_unexpected(_request: Request, _exc: Exception) -> JSONResponse:
    logger.exception("Unhandled server error")
    body = ApiResponse.fail(
        500, ApiMessageCodes.INTERNAL_ERROR, "Internal server error"
    ).model_dump()
    return JSONResponse(status_code=500, content=body)


def run() -> None:
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=settings.port, reload=False)


if __name__ == "__main__":
    run()
