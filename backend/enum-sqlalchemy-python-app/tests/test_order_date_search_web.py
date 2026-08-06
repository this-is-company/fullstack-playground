from __future__ import annotations

from datetime import datetime

from app.domain.enums import OrderStatus, PayMethod
from app.domain.order_service import OrderService
from app.web.schemas import OrderDateSearchRequest
from tests.conftest import seed_order


class TestOrderDateSearchWeb:
    def test_min_quantity_null_does_not_become_zero(self) -> None:
        body = {"orderDate": "2026-08-06", "minQuantity": None}
        request = OrderDateSearchRequest.model_validate(body)
        assert request.min_quantity is None

        # Use a throwaway session-less criteria path via OrderService.to_criteria
        # (no DB needed for this assertion).
        from unittest.mock import MagicMock

        service = OrderService(MagicMock())
        criteria = service.to_criteria(request)
        assert criteria.min_quantity is None
        assert criteria.min_quantity != 0

    def test_search_by_single_date(self, client, db) -> None:
        seed_order(
            db,
            "single-day",
            OrderStatus.PENDING,
            PayMethod.CARD,
            1,
            datetime(2026, 8, 6, 9, 0, 0),
        )
        seed_order(
            db,
            "other-day",
            OrderStatus.PENDING,
            PayMethod.CARD,
            1,
            datetime(2026, 8, 7, 9, 0, 0),
        )
        db.commit()

        resp = client.post(
            "/api/orders/search/by-date",
            json={"orderDate": "2026-08-06", "minQuantity": None},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["status"] == 200
        assert body["error"] is False
        assert len(body["result"]) == 1
        assert body["result"][0]["customerName"] == "single-day"

    def test_search_by_date_between(self, client, db) -> None:
        seed_order(
            db, "d1", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 1, 12, 0, 0)
        )
        seed_order(
            db, "d2", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 3, 12, 0, 0)
        )
        seed_order(
            db, "d3", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 10, 12, 0, 0)
        )
        db.commit()

        resp = client.post(
            "/api/orders/search/by-date-between",
            json={"fromDate": "2026-08-01", "toDate": "2026-08-03"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["error"] is False
        assert len(body["result"]) == 2

    def test_search_by_datetime_between(self, client, db) -> None:
        seed_order(
            db, "t1", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 6, 10, 0, 0)
        )
        seed_order(
            db, "t2", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 6, 15, 30, 0)
        )
        seed_order(
            db, "t3", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 6, 20, 0, 0)
        )
        db.commit()

        resp = client.post(
            "/api/orders/search/by-datetime-between",
            json={
                "fromDateTime": "2026-08-06T09:00:00",
                "toDateTime": "2026-08-06T16:00:00",
            },
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["error"] is False
        assert len(body["result"]) == 2

    def test_min_quantity_null_skips_filter_value_applies(self, client, db) -> None:
        seed_order(
            db,
            "q-small",
            OrderStatus.PENDING,
            PayMethod.CARD,
            1,
            datetime(2026, 8, 6, 11, 0, 0),
        )
        seed_order(
            db,
            "q-large",
            OrderStatus.PENDING,
            PayMethod.CARD,
            10,
            datetime(2026, 8, 6, 11, 30, 0),
        )
        db.commit()

        all_resp = client.post(
            "/api/orders/search/by-date",
            json={"orderDate": "2026-08-06", "minQuantity": None},
        )
        assert all_resp.status_code == 200
        assert all_resp.json()["error"] is False
        assert len(all_resp.json()["result"]) == 2

        filtered = client.post(
            "/api/orders/search/by-date",
            json={"orderDate": "2026-08-06", "minQuantity": 5},
        )
        assert filtered.status_code == 200
        body = filtered.json()
        assert len(body["result"]) == 1
        assert body["result"][0]["customerName"] == "q-large"
