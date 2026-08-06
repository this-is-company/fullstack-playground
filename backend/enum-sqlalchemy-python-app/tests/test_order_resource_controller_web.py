from __future__ import annotations

from app.web.exceptions import ApiMessageCodes

BASE = "/api/resource/orders"


class TestOrderResourceControllerWeb:
    def test_single_resource_flow(self, client) -> None:
        create_body = {
            "customerName": "Resource-Kim",
            "status": "P",
            "payMethod": "CARD",
            "userGrade": "B",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 1}],
        }
        created = client.put(BASE, json=create_body)
        assert created.status_code == 201
        data = created.json()
        assert data["error"] is False
        assert data["result"]["customerName"] == "Resource-Kim"
        order_id = data["result"]["id"]

        got = client.get(f"{BASE}/{order_id}")
        assert got.status_code == 200
        assert got.json()["result"]["id"] == order_id

        patch_body = {
            "customerName": "Resource-Kim-updated",
            "userGrade": "G",
            "items": [{"productName": "Book", "sku": "SKU-1", "quantity": 2}],
        }
        patched = client.patch(f"{BASE}/{order_id}", json=patch_body)
        assert patched.status_code == 200
        p = patched.json()
        assert p["result"]["customerName"] == "Resource-Kim-updated"
        assert p["result"]["status"] == "P"

        cancelled = client.post(f"{BASE}/{order_id}/cancel")
        assert cancelled.status_code == 200
        assert cancelled.json()["result"]["status"] == "C"

        deleted = client.delete(f"{BASE}/{order_id}")
        assert deleted.status_code == 200
        assert deleted.json()["error"] is False

        missing = client.get(f"{BASE}/{order_id}")
        assert missing.status_code == 404
        assert missing.json()["result"]["messageCode"] == ApiMessageCodes.ORDER_NOT_FOUND

    def test_collection_flow(self, client) -> None:
        def create(suffix: str) -> int:
            body = {
                "customerName": f"Bulk-{suffix}",
                "status": "P",
                "payMethod": "CASH",
                "userGrade": "S",
                "items": [{"productName": "Cup", "sku": "C-01", "quantity": 1}],
            }
            resp = client.put(BASE, json=body)
            assert resp.status_code == 201
            return resp.json()["result"]["id"]

        id1 = create("A")
        id2 = create("B")

        listed = client.post(BASE, json={})
        assert listed.status_code == 200
        assert listed.json()["error"] is False
        assert len(listed.json()["result"]) >= 2

        bulk_update = client.post(
            f"{BASE}/update",
            json={"ids": [id1, id2], "status": "A"},
        )
        assert bulk_update.status_code == 200
        bu = bulk_update.json()
        assert len(bu["result"]) == 2
        assert bu["result"][0]["status"] == "A"

        bulk_delete = client.post(f"{BASE}/delete", json={"ids": [id1, id2]})
        assert bulk_delete.status_code == 200
        assert bulk_delete.json()["error"] is False

        assert client.get(f"{BASE}/{id1}").status_code == 404
