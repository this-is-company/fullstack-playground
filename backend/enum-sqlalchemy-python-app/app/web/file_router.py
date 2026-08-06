from __future__ import annotations

from typing import Annotated, Any

from fastapi import APIRouter, Depends, File, Path, Query, UploadFile
from sqlalchemy.orm import Session

from app.config import settings
from app.db import get_db
from app.domain.file_service import FileAssetService
from app.domain.models import FileAsset
from app.web.envelope import EnvelopeRoute
from app.web.schemas import FileAssetResponse

router = APIRouter(
    prefix="/api/demo/files",
    tags=["File Upload Demo"],
    route_class=EnvelopeRoute,
)


def _service(db: Session = Depends(get_db)) -> FileAssetService:
    return FileAssetService(db, settings.upload_dir)


def _to_response(asset: FileAsset) -> FileAssetResponse:
    return FileAssetResponse(
        id=asset.id,
        originalName=asset.original_name,
        contentType=asset.content_type,
        sizeBytes=asset.size_bytes,
        status=asset.status,
        ownerRef=asset.owner_ref,
        createdAt=asset.created_at,
    )


@router.post("/temp", status_code=201, response_model=None)
def upload_temp(
    file: Annotated[UploadFile, File()],
    service: FileAssetService = Depends(_service),
) -> FileAssetResponse:
    return _to_response(service.upload_temp(file))


@router.get("/{id}", response_model=None)
def get_file(
    id: int = Path(...),
    service: FileAssetService = Depends(_service),
) -> FileAssetResponse:
    return _to_response(service.get(id))


@router.post("/{id}/commit", response_model=None)
def commit(
    id: int = Path(...),
    owner_ref: Annotated[str, Query(alias="ownerRef")] = ...,
    service: FileAssetService = Depends(_service),
) -> FileAssetResponse:
    return _to_response(service.commit(id, owner_ref))


@router.delete("/{id}", response_model=None)
def discard(
    id: int = Path(...),
    service: FileAssetService = Depends(_service),
) -> None:
    service.discard(id)


@router.post("/replace", response_model=None)
def replace(
    old_id: Annotated[int, Query(alias="oldId")],
    new_id: Annotated[int, Query(alias="newId")],
    owner_ref: Annotated[str, Query(alias="ownerRef")],
    service: FileAssetService = Depends(_service),
) -> FileAssetResponse:
    return _to_response(service.replace(old_id, new_id, owner_ref))


@router.post("/cleanup-orphans", response_model=None)
def cleanup_orphans(
    older_than_hours: Annotated[int, Query(alias="olderThanHours")] = 24,
    service: FileAssetService = Depends(_service),
) -> dict[str, Any]:
    deleted = service.cleanup_orphans(older_than_hours)
    return {"deletedIds": deleted, "olderThanHours": older_than_hours}
