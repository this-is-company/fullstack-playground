from __future__ import annotations

from app.web.exceptions import ApiMessageCodes


class TestOrderValidationWeb:
    def test_create_fails_when_items_list_is_null(self, client) -> None:
        body = {
            "customerName": "Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": None,
        }
        resp = client.post("/api/orders", json=body)
        assert resp.status_code == 400
        data = resp.json()
        assert data["status"] == 400
        assert data["error"] is True
        assert data["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR
        assert data["result"]["message"] == "Validation failed"
        assert "must not be null" in data["result"]["errors"]["items"]

    def test_create_fails_when_item_field_blank_or_null(self, client) -> None:
        blank_name = {
            "customerName": "Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "", "sku": "SKU-1", "quantity": 1}],
        }
        r1 = client.post("/api/orders", json=blank_name)
        assert r1.status_code == 400
        d1 = r1.json()
        assert d1["status"] == 400
        assert d1["error"] is True
        assert d1["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR

        null_sku = {
            "customerName": "Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": None, "quantity": 1}],
        }
        r2 = client.post("/api/orders", json=null_sku)
        assert r2.status_code == 400
        assert r2.json()["error"] is True

    def test_create_fails_when_required_enum_null_or_blank(self, client) -> None:
        null_status = {
            "customerName": "Kim",
            "status": None,
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        r1 = client.post("/api/orders", json=null_status)
        assert r1.status_code == 400
        assert r1.json()["error"] is True
        assert "required on create" in r1.json()["result"]["errors"]["status"]

        blank_pay = {
            "customerName": "Kim",
            "status": "P",
            "payMethod": "",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        r2 = client.post("/api/orders", json=blank_pay)
        assert r2.status_code == 400
        assert "required on create" in r2.json()["result"]["errors"]["payMethod"]

    def test_validation_groups_create_and_update_differ(self, client) -> None:
        create_with_id = {
            "id": 1,
            "customerName": "Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        r1 = client.post("/api/orders", json=create_with_id)
        assert r1.status_code == 400
        assert "must be null on create" in r1.json()["result"]["errors"]["id"]

        create_without_status = {
            "customerName": "Kim",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        r2 = client.post("/api/orders", json=create_without_status)
        assert r2.status_code == 400
        assert "required on create" in r2.json()["result"]["errors"]["status"]

        update_body = {
            "customerName": "Kim-updated",
            "userGrade": "S",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        r3 = client.put("/api/orders/99999", json=update_body)
        assert r3.status_code == 404
        d3 = r3.json()
        assert d3["status"] == 404
        assert d3["error"] is True
        assert d3["result"]["messageCode"] == ApiMessageCodes.ORDER_NOT_FOUND
        assert "order not found" in d3["result"]["message"]

    def test_create_success_converts_enums_in_response(self, client) -> None:
        body = {
            "customerName": "Lee",
            "status": "P",
            "payMethod": "TRANSFER",
            "userGrade": "G",
            "items": [{"productName": "Laptop", "sku": "L-01", "quantity": 2}],
        }
        created = client.post("/api/orders", json=body)
        assert created.status_code == 201
        data = created.json()
        assert data["status"] == 201
        assert data["error"] is False
        assert data["result"]["status"] == "P"
        assert data["result"]["statusDescription"] == "대기"
        assert data["result"]["payMethod"] == "TRANSFER"
        assert data["result"]["payMethodDescription"] == "계좌이체"
        assert data["result"]["userGrade"] == "G"
        assert data["result"]["userGradeDescription"] == "골드"
        assert data["result"]["items"][0]["productName"] == "Laptop"

        order_id = data["result"]["id"]
        got = client.get(f"/api/orders/{order_id}")
        assert got.status_code == 200
        g = got.json()
        assert g["status"] == 200
        assert g["error"] is False
        assert g["result"]["status"] == "P"
        assert g["result"]["payMethod"] == "TRANSFER"

    def test_update_allows_null_optional_enums_unlike_create(self, client) -> None:
        create_body = {
            "customerName": "Park",
            "status": "P",
            "payMethod": "CASH",
            "userGrade": "S",
            "items": [{"productName": "Cup", "sku": "C-01", "quantity": 3}],
        }
        created = client.post("/api/orders", json=create_body)
        assert created.status_code == 201
        order_id = created.json()["result"]["id"]

        update_body = {
            "customerName": "Park-updated",
            "status": "",
            "payMethod": None,
            "userGrade": "G",
            "items": [{"productName": "Cup", "sku": "C-01", "quantity": 5}],
        }
        updated = client.put(f"/api/orders/{order_id}", json=update_body)
        assert updated.status_code == 200
        data = updated.json()
        assert data["status"] == 200
        assert data["error"] is False
        assert data["result"]["customerName"] == "Park-updated"
        assert data["result"]["status"] == "P"
        assert data["result"]["payMethod"] == "CASH"
        assert data["result"]["userGrade"] == "G"

    def test_update_fails_when_items_null(self, client) -> None:
        body = {"customerName": "X", "userGrade": "B", "items": None}
        resp = client.put("/api/orders/1", json=body)
        assert resp.status_code == 400
        data = resp.json()
        assert data["status"] == 400
        assert data["error"] is True
        assert data["result"]["messageCode"] == ApiMessageCodes.VALIDATION_ERROR
