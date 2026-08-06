from __future__ import annotations

import re
import uuid
from datetime import datetime, timedelta
from pathlib import Path

from fastapi import UploadFile
from sqlalchemy.orm import Session

from app.domain.models import FileAsset, FileAssetStatus
from app.web.exceptions import ApiMessageCodes, BusinessException


class FileAssetService:
    """
    파일 수명주기:
    1) upload_temp → 디스크 저장 + DB status=TEMP
    2) commit → status=USED + owner_ref
    3) discard → TEMP 물리파일+메타 삭제
    4) replace → 새 TEMP commit 후 옛 USED 삭제
    5) cleanup_orphans → 오래된 TEMP 정리
    """

    def __init__(self, db: Session, upload_dir: str) -> None:
        self.db = db
        self.root_dir = Path(upload_dir).resolve()
        self.root_dir.mkdir(parents=True, exist_ok=True)

    def upload_temp(self, file: UploadFile | None) -> FileAsset:
        if file is None or not file.filename:
            raise BusinessException.bad_request(ApiMessageCodes.BAD_REQUEST, "file is required")

        content = file.file.read()
        if not content:
            raise BusinessException.bad_request(ApiMessageCodes.BAD_REQUEST, "file is required")

        stored_name = f"{uuid.uuid4()}_{self._sanitize(file.filename)}"
        target = self.root_dir / stored_name
        target.write_bytes(content)

        asset = FileAsset(
            original_name=file.filename,
            stored_name=stored_name,
            content_type=file.content_type,
            size_bytes=len(content),
            status=FileAssetStatus.TEMP.value,
            created_at=datetime.now(),
        )
        self.db.add(asset)
        self.db.flush()
        self.db.refresh(asset)
        return asset

    def get(self, file_id: int) -> FileAsset:
        asset = self.db.get(FileAsset, file_id)
        if asset is None:
            raise BusinessException.not_found(
                ApiMessageCodes.FILE_NOT_FOUND, f"file not found: {file_id}"
            )
        return asset

    def commit(self, file_id: int, owner_ref: str) -> FileAsset:
        asset = self.get(file_id)
        if asset.status not in (FileAssetStatus.TEMP.value, FileAssetStatus.USED.value):
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST, "invalid file status"
            )
        asset.status = FileAssetStatus.USED.value
        asset.owner_ref = owner_ref
        self.db.flush()
        self.db.refresh(asset)
        return asset

    def replace(self, old_file_id: int, new_temp_file_id: int, owner_ref: str) -> FileAsset:
        old_asset = self.get(old_file_id)
        new_asset = self.get(new_temp_file_id)
        if new_asset.status != FileAssetStatus.TEMP.value:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST, "new file must be TEMP"
            )
        new_asset.status = FileAssetStatus.USED.value
        new_asset.owner_ref = owner_ref
        self.db.flush()
        self._delete_physical_and_row(old_asset)
        self.db.refresh(new_asset)
        return new_asset

    def discard(self, file_id: int) -> None:
        asset = self.get(file_id)
        if asset.status != FileAssetStatus.TEMP.value:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST, "only TEMP files can be discarded"
            )
        self._delete_physical_and_row(asset)

    def cleanup_orphans(self, older_than_hours: int) -> list[int]:
        threshold = datetime.now() - timedelta(hours=older_than_hours)
        orphans = (
            self.db.query(FileAsset)
            .filter(
                FileAsset.status == FileAssetStatus.TEMP.value,
                FileAsset.created_at < threshold,
            )
            .all()
        )
        deleted: list[int] = []
        for orphan in orphans:
            deleted.append(orphan.id)
            self._delete_physical_and_row(orphan)
        return deleted

    def resolve_path(self, asset: FileAsset) -> Path:
        return self.root_dir / asset.stored_name

    def _delete_physical_and_row(self, asset: FileAsset) -> None:
        path = self.root_dir / asset.stored_name
        try:
            path.unlink(missing_ok=True)
        except OSError as e:
            raise RuntimeError(f"failed to delete file: {asset.stored_name}") from e
        self.db.delete(asset)
        self.db.flush()

    @staticmethod
    def _sanitize(name: str | None) -> str:
        if name is None or not name.strip():
            return "file"
        return re.sub(r"[^a-zA-Z0-9._-]", "_", name)
