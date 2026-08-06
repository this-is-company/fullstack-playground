from __future__ import annotations

from fastapi import APIRouter, Body, Depends, Path
from sqlalchemy.orm import Session

from app.db import get_db
from app.domain.order_service import OrderService
from app.web.envelope import EnvelopeRoute
from app.web.schemas import (
    OrderDateSearchRequest,
    OrderIdsRequest,
    OrderRequest,
    OrderResponse,
)

router = APIRouter(
    prefix="/api/resource/orders", tags=["Order Resource"], route_class=EnvelopeRoute
)


def _service(db: Session = Depends(get_db)) -> OrderService:
    return OrderService(db)


@router.post("", response_model=None)
def list_orders(
    request: OrderDateSearchRequest | None = Body(default=None),
    service: OrderService = Depends(_service),
) -> list[OrderResponse]:
    return service.list(request or OrderDateSearchRequest())


@router.get("/{id}", response_model=None)
def get_order(
    id: int = Path(...),
    service: OrderService = Depends(_service),
) -> OrderResponse:
    return service.get(id)


@router.put("", status_code=201, response_model=None)
def create(
    request: OrderRequest,
    service: OrderService = Depends(_service),
) -> OrderResponse:
    request.validate_for("create")
    return service.create(request)


@router.patch("/{id}", response_model=None)
def update(
    request: OrderRequest,
    id: int = Path(...),
    service: OrderService = Depends(_service),
) -> OrderResponse:
    request.id = id
    request.validate_for("update")
    return service.update(request)


@router.delete("/{id}", response_model=None)
def delete_order(
    id: int = Path(...),
    service: OrderService = Depends(_service),
) -> None:
    service.delete(id)


@router.post("/{id}/cancel", response_model=None)
def cancel(
    id: int = Path(...),
    service: OrderService = Depends(_service),
) -> OrderResponse:
    return service.cancel(id)


@router.post("/update", response_model=None)
def update_many(
    request: OrderIdsRequest,
    service: OrderService = Depends(_service),
) -> list[OrderResponse]:
    return service.update_status_many(request.ids, request.status)


@router.post("/delete", response_model=None)
def delete_many(
    request: OrderIdsRequest,
    service: OrderService = Depends(_service),
) -> None:
    service.delete_many(request.ids)
