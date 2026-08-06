from __future__ import annotations

from app.web.exceptions import ApiMessageCodes


class TestRequestPatternValidationWeb:
    def test_local_date_pattern_and_calendar_validated(self, client) -> None:
        r1 = client.post(
            "/api/orders/search/by-date",
            json={"orderDate": "06/08/2026"},
        )
        assert r1.status_code == 400
        d1 = r1.json()
        assert d1["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR
        assert "yyyy-MM-dd" in d1["result"]["errors"]["orderDate"]

        r2 = client.post(
            "/api/orders/search/by-date",
            json={"orderDate": "2026-02-30"},
        )
        assert r2.status_code == 400
        assert "yyyy-MM-dd" in r2.json()["result"]["errors"]["orderDate"]

    def test_local_datetime_pattern_validated(self, client) -> None:
        r1 = client.post(
            "/api/orders/search/by-datetime-between",
            json={
                "fromDateTime": "2026-08-06 09:00:00",
                "toDateTime": "2026-08-06T18:00:00",
            },
        )
        assert r1.status_code == 400
        assert "yyyy-MM-dd'T'HH:mm:ss" in r1.json()["result"]["errors"]["fromDateTime"]

        r2 = client.post(
            "/api/orders/search/by-datetime-between",
            json={
                "fromDateTime": "2026-08-06T09:00",
                "toDateTime": "2026-08-06T18:00:00",
            },
        )
        assert r2.status_code == 400
        assert "yyyy-MM-dd'T'HH:mm:ss" in r2.json()["result"]["errors"]["fromDateTime"]

    def test_string_rejects_special_characters(self, client) -> None:
        body = {
            "customerName": "Kim<script>",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        r1 = client.post("/api/orders", json=body)
        assert r1.status_code == 400
        d1 = r1.json()
        assert d1["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR
        assert "special characters" in d1["result"]["errors"]["customerName"]

        sku_body = {
            "customerName": "Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU@1!", "quantity": 1}],
        }
        r2 = client.post("/api/orders", json=sku_body)
        assert r2.status_code == 400
        assert "special characters" in r2.json()["result"]["errors"]["items[0].sku"]

    def test_promo_code_pattern_validated(self, client) -> None:
        body = {
            "customerName": "Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "promoCode": "PROMO@BAD!",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        resp = client.post("/api/orders", json=body)
        assert resp.status_code == 400
        assert "promoCode must match pattern" in resp.json()["result"]["errors"]["promoCode"]
