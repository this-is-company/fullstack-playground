from __future__ import annotations

import json
from datetime import date, datetime
from typing import Any, List, Optional

from fastapi import APIRouter, Depends, Query, Request
from fastapi.exceptions import RequestValidationError
from pydantic import TypeAdapter, ValidationError

from app.common.date_strings import DateStrings
from app.web.envelope import EnvelopeRoute
from app.web.schemas import (
    GetValidationItemFilter,
    GetValidationItemView,
    GetValidationParsedResponse,
    ValidationFailed,
)
from app.web.validators import local_date_string, local_datetime_string

router = APIRouter(
    prefix="/api/demo/get-validation",
    tags=["GET Validation Demo"],
    route_class=EnvelopeRoute,
)

_ITEM_LIST_ADAPTER = TypeAdapter(List[GetValidationItemFilter])


def _validate_optional_date(value: Optional[str], field: str) -> Optional[str]:
    try:
        return local_date_string(value)
    except ValueError as e:
        raise ValidationFailed({field: str(e)}) from e


def _validate_optional_datetime(value: Optional[str], field: str) -> Optional[str]:
    try:
        return local_datetime_string(value)
    except ValueError as e:
        raise ValidationFailed({field: str(e)}) from e


def _items_validation_errors(exc: ValidationError) -> dict[str, str]:
    errors: dict[str, str] = {}
    for err in exc.errors():
        loc = err.get("loc", ())
        parts: list[str] = ["items"]
        for p in loc:
            if isinstance(p, int):
                parts[-1] = f"{parts[-1]}[{p}]"
            else:
                parts.append(str(p))
        key = ".".join(parts)
        msg = err.get("msg", "")
        if msg.startswith("Value error, "):
            msg = msg[len("Value error, ") :]
        errors[key] = msg
    return errors


def _parse_items_json(raw: Optional[str]) -> List[GetValidationItemFilter]:
    if raw is None or not str(raw).strip():
        raise ValidationFailed({"items": "items must not be null or empty"})
    try:
        data = json.loads(raw)
    except json.JSONDecodeError as e:
        raise ValidationFailed({"items": "items must be a valid JSON array"}) from e
    if not isinstance(data, list):
        raise ValidationFailed({"items": "items must be a valid JSON array"})
    if len(data) == 0:
        raise ValidationFailed({"items": "items must not be null or empty"})
    if any(item is None for item in data):
        raise ValidationFailed({"items": "must not contain null elements"})
    try:
        return _ITEM_LIST_ADAPTER.validate_python(data)
    except ValidationError as e:
        raise ValidationFailed(_items_validation_errors(e)) from e


def _parse_bracket_items(request: Request) -> Optional[List[GetValidationItemFilter]]:
    """Spring-style items[0].sku / items[0].quantity from query string."""
    collected: dict[int, dict[str, Any]] = {}
    for key, value in request.query_params.multi_items():
        if not key.startswith("items[") or "]." not in key:
            continue
        try:
            idx_s, field = key[len("items[") :].split("].", 1)
            idx = int(idx_s)
        except ValueError:
            continue
        collected.setdefault(idx, {})[field] = value
    if not collected:
        return None
    ordered = [collected[i] for i in sorted(collected)]
    try:
        return _ITEM_LIST_ADAPTER.validate_python(ordered)
    except ValidationError as e:
        raise ValidationFailed(_items_validation_errors(e)) from e


class SearchQuery:
    __slots__ = (
        "ids",
        "items",
        "min_quantity",
        "page",
        "order_date",
        "from_date",
        "to_date",
        "from_date_time",
        "to_date_time",
    )

    def __init__(
        self,
        ids: List[int],
        items: List[GetValidationItemFilter],
        min_quantity: Optional[int],
        page: Optional[int],
        order_date: Optional[str],
        from_date: Optional[str],
        to_date: Optional[str],
        from_date_time: Optional[str],
        to_date_time: Optional[str],
    ) -> None:
        self.ids = ids
        self.items = items
        self.min_quantity = min_quantity
        self.page = page
        self.order_date = order_date
        self.from_date = from_date
        self.to_date = to_date
        self.from_date_time = from_date_time
        self.to_date_time = to_date_time


def parse_search_query(
    request: Request,
    ids: Optional[List[int]] = Query(default=None),
    items: Optional[str] = Query(default=None),
    min_quantity: Optional[int] = Query(default=None, alias="minQuantity"),
    page: Optional[int] = Query(default=None),
    order_date: Optional[str] = Query(default=None, alias="orderDate"),
    from_date: Optional[str] = Query(default=None, alias="fromDate"),
    to_date: Optional[str] = Query(default=None, alias="toDate"),
    from_date_time: Optional[str] = Query(default=None, alias="fromDateTime"),
    to_date_time: Optional[str] = Query(default=None, alias="toDateTime"),
) -> SearchQuery:
    errors: dict[str, str] = {}

    if ids is None or len(ids) == 0:
        errors["ids"] = "ids must not be null or empty"
    elif any(i is None for i in ids):
        errors["ids"] = "must not contain null elements"

    if min_quantity is not None and min_quantity < 0:
        errors["minQuantity"] = "minQuantity must be >= 0"
    if page is not None and page <= 0:
        errors["page"] = "page must be positive"

    try:
        order_date = _validate_optional_date(order_date, "orderDate")
        from_date = _validate_optional_date(from_date, "fromDate")
        to_date = _validate_optional_date(to_date, "toDate")
        from_date_time = _validate_optional_datetime(from_date_time, "fromDateTime")
        to_date_time = _validate_optional_datetime(to_date_time, "toDateTime")
    except ValidationFailed as e:
        errors.update(e.errors)

    parsed_items: Optional[List[GetValidationItemFilter]] = None
    try:
        bracket = _parse_bracket_items(request)
        if bracket is not None:
            parsed_items = bracket
        elif items is not None:
            parsed_items = _parse_items_json(items)
        else:
            errors["items"] = "items must not be null or empty"
    except ValidationFailed as e:
        errors.update(e.errors)

    if errors:
        raise ValidationFailed(errors)

    assert ids is not None and parsed_items is not None
    return SearchQuery(
        ids=ids,
        items=parsed_items,
        min_quantity=min_quantity,
        page=page,
        order_date=order_date,
        from_date=from_date,
        to_date=to_date,
        from_date_time=from_date_time,
        to_date_time=to_date_time,
    )


@router.get("/search", response_model=None)
def search(query: SearchQuery = Depends(parse_search_query)) -> GetValidationParsedResponse:
    return GetValidationParsedResponse(
        ids=query.ids,
        items=[
            GetValidationItemView(sku=i.sku, quantity=i.quantity) for i in query.items
        ],
        minQuantity=query.min_quantity,
        page=query.page,
        orderDate=DateStrings.to_local_date(query.order_date),
        fromDate=DateStrings.to_local_date(query.from_date),
        toDate=DateStrings.to_local_date(query.to_date),
        fromDateTime=DateStrings.to_local_datetime(query.from_date_time),
        toDateTime=DateStrings.to_local_datetime(query.to_date_time),
        note="validated as strings then converted via DateStrings",
    )


def _strict_date(value: Optional[str]) -> Optional[date]:
    if value is None or not str(value).strip():
        return None
    try:
        return datetime.strptime(value.strip(), "%Y-%m-%d").date()
    except ValueError as e:
        raise RequestValidationError(
            [
                {
                    "type": "date_parsing",
                    "loc": ("query", "orderDate"),
                    "msg": f"Invalid date format: {value}",
                    "input": value,
                }
            ]
        ) from e


def _strict_datetime(value: Optional[str], field: str) -> Optional[datetime]:
    if value is None or not str(value).strip():
        return None
    try:
        return datetime.strptime(value.strip(), "%Y-%m-%dT%H:%M:%S")
    except ValueError as e:
        raise RequestValidationError(
            [
                {
                    "type": "datetime_parsing",
                    "loc": ("query", field),
                    "msg": f"Invalid datetime format: {value}",
                    "input": value,
                }
            ]
        ) from e


@router.get("/typed-dates", response_model=None)
def typed_dates(
    order_date: Optional[str] = Query(default=None, alias="orderDate"),
    from_date_time: Optional[str] = Query(default=None, alias="fromDateTime"),
    to_date_time: Optional[str] = Query(default=None, alias="toDateTime"),
    min_quantity: Optional[int] = Query(default=None, alias="minQuantity", ge=0),
) -> dict[str, Any]:
    od = _strict_date(order_date)
    fdt = _strict_datetime(from_date_time, "fromDateTime")
    tdt = _strict_datetime(to_date_time, "toDateTime")
    return {
        "orderDate": od.isoformat() if od else None,
        "orderDateType": "LocalDate" if od is not None else None,
        "fromDateTime": fdt.strftime("%Y-%m-%dT%H:%M:%S") if fdt else None,
        "fromDateTimeType": "LocalDateTime" if fdt is not None else None,
        "toDateTime": tdt.strftime("%Y-%m-%dT%H:%M:%S") if tdt else None,
        "minQuantity": min_quantity,
        "note": "bound directly to LocalDate/LocalDateTime by Spring",
    }


@router.get("/required-ids", response_model=None)
def required_ids(
    ids: Optional[List[int]] = Query(default=None),
) -> dict[str, Any]:
    if ids is None or len(ids) == 0:
        raise ValidationFailed({"ids": "ids must not be null or empty"})
    if any(i is None for i in ids):
        raise ValidationFailed({"ids": "must not contain null elements"})
    return {
        "ids": ids,
        "note": "List<@NotNull Long> + custom @NotEmptyList",
    }
