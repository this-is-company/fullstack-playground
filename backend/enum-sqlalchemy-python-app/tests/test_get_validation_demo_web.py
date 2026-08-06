from __future__ import annotations

import json

from app.web.exceptions import ApiMessageCodes


class TestGetValidationDemoWeb:
    def test_search_ok_converts_dates(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/search",
            params=[
                ("ids", "1"),
                ("ids", "2"),
                ("items[0].sku", "ABC-1"),
                ("items[0].quantity", "3"),
                ("minQuantity", "5"),
                ("orderDate", "2026-08-06"),
                ("fromDateTime", "2026-08-06T09:00:00"),
            ],
        )
        assert resp.status_code == 200
        data = resp.json()
        assert data["error"] is False
        assert data["result"]["ids"][0] == 1
        assert data["result"]["items"][0]["sku"] == "ABC-1"
        assert data["result"]["minQuantity"] == 5
        assert data["result"]["orderDate"] == "2026-08-06"
        assert data["result"]["fromDateTime"] == "2026-08-06T09:00:00"

    def test_search_ok_with_items_json(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/search",
            params=[
                ("ids", "1"),
                ("ids", "2"),
                ("items", json.dumps([{"sku": "ABC-1", "quantity": 3}])),
                ("minQuantity", "5"),
                ("orderDate", "2026-08-06"),
                ("fromDateTime", "2026-08-06T09:00:00"),
            ],
        )
        assert resp.status_code == 200
        assert resp.json()["result"]["items"][0]["sku"] == "ABC-1"

    def test_search_missing_ids(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/search",
            params=[
                ("items[0].sku", "ABC"),
                ("items[0].quantity", "1"),
            ],
        )
        assert resp.status_code == 400
        data = resp.json()
        assert data["error"] is True
        assert data["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR
        assert "ids" in data["result"]["errors"]

    def test_search_item_quantity_missing(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/search",
            params=[
                ("ids", "1"),
                ("items[0].sku", "ABC"),
            ],
        )
        assert resp.status_code == 400
        assert "items[0].quantity" in resp.json()["result"]["errors"]

    def test_search_bad_date(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/search",
            params=[
                ("ids", "1"),
                ("items[0].sku", "ABC"),
                ("items[0].quantity", "1"),
                ("orderDate", "2026-13-40"),
            ],
        )
        assert resp.status_code == 400
        assert resp.json()["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR

    def test_search_negative_min_quantity(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/search",
            params=[
                ("ids", "1"),
                ("items[0].sku", "ABC"),
                ("items[0].quantity", "1"),
                ("minQuantity", "-1"),
            ],
        )
        assert resp.status_code == 400
        assert "minQuantity" in resp.json()["result"]["errors"]

    def test_typed_dates_ok(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/typed-dates",
            params={
                "orderDate": "2026-08-06",
                "fromDateTime": "2026-08-06T09:00:00",
                "minQuantity": "3",
            },
        )
        assert resp.status_code == 200
        result = resp.json()["result"]
        assert result["orderDate"] == "2026-08-06"
        assert result["orderDateType"] == "LocalDate"
        assert result["fromDateTimeType"] == "LocalDateTime"
        assert result["minQuantity"] == 3

    def test_typed_dates_bad_format(self, client) -> None:
        resp = client.get(
            "/api/demo/get-validation/typed-dates",
            params={"orderDate": "08/06/2026"},
        )
        assert resp.status_code == 400
        assert resp.json()["error"] is True

    def test_required_ids_empty(self, client) -> None:
        resp = client.get("/api/demo/get-validation/required-ids")
        assert resp.status_code == 400
        assert resp.json()["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR
