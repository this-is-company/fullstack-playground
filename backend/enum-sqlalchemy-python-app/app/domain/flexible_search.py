from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta

from app.common.date_strings import DateStrings
from app.domain.enums import OrderStatus, PayMethod
from app.web.schemas import OrderFlexibleSearchRequest


@dataclass
class OrderFlexibleSearchCriteria:
    """유연 검색용 typed criteria. null 필드는 조건에 넣지 않는다."""

    customer_name: str | None = None
    status: OrderStatus | None = None
    pay_method: PayMethod | None = None
    min_quantity: int | None = None
    created_from: datetime | None = None
    created_to: datetime | None = None
    created_to_inclusive: bool = False

    def is_empty(self) -> bool:
        return (
            self.customer_name is None
            and self.status is None
            and self.pay_method is None
            and self.min_quantity is None
            and self.created_from is None
            and self.created_to is None
        )


class OrderFlexibleSearchCriteriaBuilder:
    """
    Request(전부 optional) → typed OrderFlexibleSearchCriteria.
    빈 문자열/null 은 조건에서 제외한다.
    """

    @staticmethod
    def from_request(
        request: OrderFlexibleSearchRequest | None,
    ) -> OrderFlexibleSearchCriteria:
        criteria = OrderFlexibleSearchCriteria()
        if request is None:
            return criteria

        if _has_text(request.customer_name):
            assert request.customer_name is not None
            criteria.customer_name = request.customer_name.strip()
        if request.status is not None:
            criteria.status = request.status
        if request.pay_method is not None:
            criteria.pay_method = request.pay_method
        if request.min_quantity is not None:
            criteria.min_quantity = request.min_quantity

        _apply_created_at_range(criteria, request)
        return criteria


def _apply_created_at_range(
    criteria: OrderFlexibleSearchCriteria,
    request: OrderFlexibleSearchRequest,
) -> None:
    """우선순위: orderDate > fromDate/toDate > fromDateTime/toDateTime
    (동시에 여러 날짜 그룹이 와도 하나만 적용)"""
    if _has_text(request.order_date):
        day = DateStrings.to_local_date(request.order_date)
        assert day is not None
        criteria.created_from = datetime.combine(day, datetime.min.time())
        criteria.created_to = datetime.combine(day + timedelta(days=1), datetime.min.time())
        criteria.created_to_inclusive = False
        return

    has_date_bound = _has_text(request.from_date) or _has_text(request.to_date)
    if has_date_bound:
        if _has_text(request.from_date):
            d = DateStrings.to_local_date(request.from_date)
            assert d is not None
            criteria.created_from = datetime.combine(d, datetime.min.time())
        if _has_text(request.to_date):
            d = DateStrings.to_local_date(request.to_date)
            assert d is not None
            criteria.created_to = datetime.combine(d + timedelta(days=1), datetime.min.time())
            criteria.created_to_inclusive = False
        return

    if _has_text(request.from_date_time) or _has_text(request.to_date_time):
        if _has_text(request.from_date_time):
            criteria.created_from = DateStrings.to_local_datetime(request.from_date_time)
        if _has_text(request.to_date_time):
            criteria.created_to = DateStrings.to_local_datetime(request.to_date_time)
            criteria.created_to_inclusive = True


def _has_text(value: str | None) -> bool:
    return value is not None and bool(str(value).strip())
