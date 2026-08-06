from __future__ import annotations

import json
from typing import Any, Callable, Coroutine

from fastapi import Request, Response
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from fastapi.routing import APIRoute

from app.web.api_response import ApiResponse


def dump_result(result: Any) -> Any:
    if result is None:
        return None
    if hasattr(result, "model_dump"):
        return result.model_dump(by_alias=True, mode="json")
    if isinstance(result, list):
        return [dump_result(item) for item in result]
    return jsonable_encoder(result)


class EnvelopeRoute(APIRoute):
    """Controller 정상 응답을 ApiResponse 로 감싼다."""

    def get_route_handler(self) -> Callable[[Request], Coroutine[Any, Any, Response]]:
        original = super().get_route_handler()

        async def custom(request: Request) -> Response:
            response = await original(request)
            if not isinstance(response, JSONResponse):
                if response.status_code < 400 and not response.body:
                    status = response.status_code or 200
                    return JSONResponse(
                        status_code=status,
                        content=ApiResponse.ok(status, None).model_dump(),
                    )
                return response

            try:
                raw = json.loads(response.body) if response.body else None
            except Exception:
                return response

            if (
                isinstance(raw, dict)
                and "error" in raw
                and "status" in raw
                and "result" in raw
            ):
                return response

            status = response.status_code
            return JSONResponse(
                status_code=status,
                content=ApiResponse.ok(status, raw).model_dump(),
            )

        return custom
