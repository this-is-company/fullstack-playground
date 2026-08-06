from __future__ import annotations


class TestFileUploadDemoWeb:
    def test_upload_temp_then_commit(self, client) -> None:
        created = client.post(
            "/api/demo/files/temp",
            files={"file": ("hello.txt", b"hello", "text/plain")},
        )
        assert created.status_code == 201
        body = created.json()
        assert body["result"]["status"] == "TEMP"
        file_id = body["result"]["id"]

        committed = client.post(
            f"/api/demo/files/{file_id}/commit",
            params={"ownerRef": "order:1"},
        )
        assert committed.status_code == 200
        result = committed.json()["result"]
        assert result["status"] == "USED"
        assert result["ownerRef"] == "order:1"

    def test_discard_temp(self, client) -> None:
        created = client.post(
            "/api/demo/files/temp",
            files={"file": ("bye.txt", b"bye", "text/plain")},
        )
        assert created.status_code == 201
        file_id = created.json()["result"]["id"]

        discarded = client.delete(f"/api/demo/files/{file_id}")
        assert discarded.status_code == 200

        missing = client.get(f"/api/demo/files/{file_id}")
        assert missing.status_code == 404

    def test_replace_file(self, client) -> None:
        old_id = self._upload(client, "old.txt", b"old")
        client.post(
            f"/api/demo/files/{old_id}/commit",
            params={"ownerRef": "order:9"},
        ).raise_for_status()

        new_id = self._upload(client, "new.txt", b"new")

        replaced = client.post(
            "/api/demo/files/replace",
            params={
                "oldId": str(old_id),
                "newId": str(new_id),
                "ownerRef": "order:9",
            },
        )
        assert replaced.status_code == 200
        result = replaced.json()["result"]
        assert result["id"] == new_id
        assert result["status"] == "USED"

        missing = client.get(f"/api/demo/files/{old_id}")
        assert missing.status_code == 404

    @staticmethod
    def _upload(client, name: str, content: bytes) -> int:
        resp = client.post(
            "/api/demo/files/temp",
            files={"file": (name, content, "text/plain")},
        )
        assert resp.status_code == 201
        body = resp.json()
        assert body["result"]["status"] == "TEMP"
        return body["result"]["id"]
