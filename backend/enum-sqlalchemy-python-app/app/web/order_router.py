from __future__ import annotations

from fastapi import APIRouter, Body, Depends, Path
from sqlalchemy.orm import Session

from app.db import get_db
from app.domain.order_service import OrderService
from app.web.envelope import EnvelopeRoute
from app.web.schemas import (
    OrderDateSearchRequest,
    OrderFlexibleSearchRequest,
    OrderRequest,
    OrderResponse,
)

router = APIRouter(prefix="/api/orders", tags=["Order"], route_class=EnvelopeRoute)


def _service(db: Session = Depends(get_db)) -> OrderService:
    return OrderService(db)


@router.post("", status_code=201, response_model=None)
def create(
    request: OrderRequest,
    service: OrderService = Depends(_service),
) -> OrderResponse:
    request.validate_for("create")
    return service.create(request)


@router.put("/{id}", response_model=None)
def update(
    request: OrderRequest,
    id: int = Path(...),
    service: OrderService = Depends(_service),
) -> OrderResponse:
    request.id = id
    request.validate_for("update")
    return service.update(request)


@router.get("/{id}", response_model=None)
def get_order(
    id: int = Path(...),
    service: OrderService = Depends(_service),
) -> OrderResponse:
    return service.get(id)


@router.post("/search", response_model=None)
def search_flexible(
    request: OrderFlexibleSearchRequest | None = Body(default=None),
    service: OrderService = Depends(_service),
) -> list[OrderResponse]:
    return service.search_flexible(request or OrderFlexibleSearchRequest())


@router.post("/search/by-date", response_model=None)
def search_by_date(
    request: OrderDateSearchRequest,
    service: OrderService = Depends(_service),
) -> list[OrderResponse]:
    return service.search_by_single_date(request)


@router.post("/search/by-date-between", response_model=None)
def search_by_date_between(
    request: OrderDateSearchRequest,
    service: OrderService = Depends(_service),
) -> list[OrderResponse]:
    return service.search_by_date_between(request)


@router.post("/search/by-datetime-between", response_model=None)
def search_by_datetime_between(
    request: OrderDateSearchRequest,
    service: OrderService = Depends(_service),
) -> list[OrderResponse]:
    return service.search_by_datetime_between(request)
