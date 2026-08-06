from __future__ import annotations

from datetime import datetime

from app.domain.enums import OrderStatus, PayMethod
from tests.conftest import seed_order


class TestOrderFlexibleSearchWeb:
    def test_empty_criteria_returns_all(self, client, db) -> None:
        seed_order(
            db, "Flex-A", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 8, 1, 10, 0)
        )
        seed_order(
            db, "Flex-B", OrderStatus.PAID, PayMethod.CASH, 5, datetime(2026, 8, 5, 10, 0)
        )
        db.commit()

        r1 = client.post("/api/orders/search", json={})
        assert r1.status_code == 200
        body1 = r1.json()
        assert body1["error"] is False
        assert body1["status"] == 200
        assert len(body1["result"]) >= 2

        r2 = client.post(
            "/api/orders/search",
            json={
                "customerName": None,
                "status": None,
                "payMethod": None,
                "minQuantity": None,
                "orderDate": "",
                "fromDate": None,
                "toDate": "",
                "fromDateTime": None,
                "toDateTime": None,
            },
        )
        assert r2.status_code == 200
        assert len(r2.json()["result"]) >= 2

    def test_combined_optional_filters(self, client, db) -> None:
        seed_order(
            db,
            "Alpha-Kim",
            OrderStatus.PENDING,
            PayMethod.CARD,
            1,
            datetime(2026, 8, 2, 9, 0),
        )
        seed_order(
            db,
            "Alpha-Kim",
            OrderStatus.PENDING,
            PayMethod.CARD,
            10,
            datetime(2026, 8, 2, 15, 0),
        )
        seed_order(
            db,
            "Beta-Lee",
            OrderStatus.PAID,
            PayMethod.CASH,
            10,
            datetime(2026, 8, 2, 12, 0),
        )
        db.commit()

        resp = client.post(
            "/api/orders/search",
            json={
                "customerName": "Kim",
                "status": "P",
                "payMethod": "CARD",
                "minQuantity": 5,
                "orderDate": "2026-08-02",
            },
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["error"] is False
        assert len(body["result"]) == 1
        assert body["result"][0]["customerName"] == "Alpha-Kim"
        assert body["result"][0]["items"][0]["quantity"] == 10

    def test_date_between_only(self, client, db) -> None:
        seed_order(
            db, "D1", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 7, 1, 10, 0)
        )
        seed_order(
            db, "D2", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 7, 5, 10, 0)
        )
        seed_order(
            db, "D3", OrderStatus.PENDING, PayMethod.CARD, 1, datetime(2026, 7, 20, 10, 0)
        )
        db.commit()

        resp = client.post(
            "/api/orders/search",
            json={"fromDate": "2026-07-01", "toDate": "2026-07-05"},
        )
        assert resp.status_code == 200
        body = resp.json()
        assert body["error"] is False
        assert len(body["result"]) == 2
