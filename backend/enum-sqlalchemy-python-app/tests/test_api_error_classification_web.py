from __future__ import annotations

import pytest

from app.main import app
from app.web.exceptions import ApiMessageCodes, BusinessException


class TestApiErrorClassificationWeb:
    def test_business_error_is_4xx(self, client) -> None:
        resp = client.get("/api/orders/999999")
        assert resp.status_code == 404
        data = resp.json()
        assert data["status"] == 404
        assert data["error"] is True
        assert data["result"]["messageCode"] == ApiMessageCodes.ORDER_NOT_FOUND
        assert "order not found" in data["result"]["message"]

    def test_server_error_is_5xx(self, client) -> None:
        def boom() -> None:
            raise RuntimeError("secret db password leaked")

        app.add_api_route("/__test__/boom", boom, methods=["GET"])
        try:
            resp = client.get("/__test__/boom")
            assert resp.status_code == 500
            data = resp.json()
            assert data["status"] == 500
            assert data["error"] is True
            assert data["result"]["messageCode"] == ApiMessageCodes.INTERNAL_ERROR
            assert data["result"]["message"] == "Internal server error"
        finally:
            # Remove the test-only route so later tests are unaffected.
            app.router.routes = [
                r
                for r in app.router.routes
                if getattr(r, "path", None) != "/__test__/boom"
            ]

    def test_business_exception_rejects_5xx_status(self) -> None:
        with pytest.raises(ValueError, match="4xx"):
            BusinessException(500, "X", "y")
