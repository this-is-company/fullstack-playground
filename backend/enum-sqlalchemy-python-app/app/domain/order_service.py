from __future__ import annotations

from dataclasses import dataclass
from datetime import date, datetime, timedelta

from sqlalchemy.orm import Session

from app.common.date_strings import DateStrings
from app.domain.enums import OrderStatus
from app.domain.flexible_search import OrderFlexibleSearchCriteriaBuilder
from app.domain.models import Order, OrderItem
from app.domain.order_query import OrderQuery
from app.web.exceptions import ApiMessageCodes, BusinessException
from app.web.schemas import (
    OrderDateSearchRequest,
    OrderFlexibleSearchRequest,
    OrderItemResponse,
    OrderRequest,
    OrderResponse,
)


@dataclass
class OrderSearchCriteria:
    order_date: date | None = None
    from_date: date | None = None
    to_date: date | None = None
    from_date_time: datetime | None = None
    to_date_time: datetime | None = None
    min_quantity: int | None = None


class OrderService:
    def __init__(self, db: Session) -> None:
        self.db = db
        self.query = OrderQuery(db)

    def create(self, request: OrderRequest) -> OrderResponse:
        order = self._to_entity(request)
        if order.created_at is None:
            order.created_at = datetime.now()
        self._apply_items(order, request.items)
        self.db.add(order)
        self.db.flush()
        return self.get(order.id)

    def create_with_created_at(
        self, request: OrderRequest, created_at: datetime
    ) -> OrderResponse:
        order = self._to_entity(request)
        order.created_at = created_at
        self._apply_items(order, request.items)
        self.db.add(order)
        self.db.flush()
        return self.get(order.id)

    def update(self, request: OrderRequest) -> OrderResponse:
        assert request.id is not None
        existing = self.query.find_with_items_by_id(request.id)
        if existing is None:
            raise BusinessException.not_found(
                ApiMessageCodes.ORDER_NOT_FOUND,
                f"order not found: {request.id}",
            )

        existing.customer_name = request.customer_name  # type: ignore[assignment]
        existing.user_grade = request.user_grade  # type: ignore[assignment]
        if request.status is not None:
            existing.status = request.status
        if request.pay_method is not None:
            existing.pay_method = request.pay_method
        self._apply_items(existing, request.items)
        self.db.flush()
        return self.get(existing.id)

    def get(self, order_id: int) -> OrderResponse:
        order = self.query.find_with_items_by_id(order_id)
        if order is None:
            raise BusinessException.not_found(
                ApiMessageCodes.ORDER_NOT_FOUND,
                f"order not found: {order_id}",
            )
        return self._to_response(order)

    def delete(self, order_id: int) -> None:
        order = self.db.get(Order, order_id)
        if order is None:
            raise BusinessException.not_found(
                ApiMessageCodes.ORDER_NOT_FOUND,
                f"order not found: {order_id}",
            )
        self.db.delete(order)
        self.db.flush()

    def cancel(self, order_id: int) -> OrderResponse:
        existing = self.db.get(Order, order_id)
        if existing is None:
            raise BusinessException.not_found(
                ApiMessageCodes.ORDER_NOT_FOUND,
                f"order not found: {order_id}",
            )
        if existing.status == OrderStatus.CANCELLED:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST,
                f"order already cancelled: {order_id}",
            )
        existing.status = OrderStatus.CANCELLED
        self.db.flush()
        return self.get(order_id)

    def list(self, request: OrderDateSearchRequest) -> list[OrderResponse]:
        if request.order_date and str(request.order_date).strip():
            return self.search_by_single_date(request)
        if (
            request.from_date
            and str(request.from_date).strip()
            and request.to_date
            and str(request.to_date).strip()
        ):
            return self.search_by_date_between(request)
        if (
            request.from_date_time
            and str(request.from_date_time).strip()
            and request.to_date_time
            and str(request.to_date_time).strip()
        ):
            return self.search_by_datetime_between(request)
        return self._map_list(self.query.find_all_with_items())

    def search_flexible(
        self, request: OrderFlexibleSearchRequest | None
    ) -> list[OrderResponse]:
        criteria = OrderFlexibleSearchCriteriaBuilder.from_request(request)
        return self._map_list(self.query.search_flexible(criteria))

    def update_status_many(
        self, ids: list[int], status: OrderStatus | None
    ) -> list[OrderResponse]:
        if status is None:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST,
                "status is required for bulk update",
            )
        for order_id in ids:
            existing = self.db.get(Order, order_id)
            if existing is None:
                raise BusinessException.not_found(
                    ApiMessageCodes.ORDER_NOT_FOUND,
                    f"order not found: {order_id}",
                )
            existing.status = status
        self.db.flush()
        return self._map_list(self.query.find_by_ids_with_items(ids))

    def delete_many(self, ids: list[int] | None) -> None:
        if not ids:
            return
        for order_id in ids:
            order = self.db.get(Order, order_id)
            if order is not None:
                self.db.delete(order)
        self.db.flush()

    def search_by_single_date(self, request: OrderDateSearchRequest) -> list[OrderResponse]:
        criteria = self.to_criteria(request)
        if criteria.order_date is None:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST, "orderDate is required"
            )
        from_ = datetime.combine(criteria.order_date, datetime.min.time())
        to = datetime.combine(criteria.order_date + timedelta(days=1), datetime.min.time())
        return self._map_list(
            self.query.search_by_created_at(from_, to, False, criteria.min_quantity)
        )

    def search_by_date_between(self, request: OrderDateSearchRequest) -> list[OrderResponse]:
        criteria = self.to_criteria(request)
        if criteria.from_date is None or criteria.to_date is None:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST, "fromDate and toDate are required"
            )
        from_ = datetime.combine(criteria.from_date, datetime.min.time())
        to = datetime.combine(criteria.to_date + timedelta(days=1), datetime.min.time())
        return self._map_list(
            self.query.search_by_created_at(from_, to, False, criteria.min_quantity)
        )

    def search_by_datetime_between(
        self, request: OrderDateSearchRequest
    ) -> list[OrderResponse]:
        criteria = self.to_criteria(request)
        if criteria.from_date_time is None or criteria.to_date_time is None:
            raise BusinessException.bad_request(
                ApiMessageCodes.BAD_REQUEST,
                "fromDateTime and toDateTime are required",
            )
        return self._map_list(
            self.query.search_by_created_at(
                criteria.from_date_time,
                criteria.to_date_time,
                True,
                criteria.min_quantity,
            )
        )

    def to_criteria(self, request: OrderDateSearchRequest) -> OrderSearchCriteria:
        return OrderSearchCriteria(
            order_date=DateStrings.to_local_date(request.order_date),
            from_date=DateStrings.to_local_date(request.from_date),
            to_date=DateStrings.to_local_date(request.to_date),
            from_date_time=DateStrings.to_local_datetime(request.from_date_time),
            to_date_time=DateStrings.to_local_datetime(request.to_date_time),
            min_quantity=request.min_quantity,
        )

    def _map_list(self, orders: list[Order]) -> list[OrderResponse]:
        return [self._to_response(o) for o in orders]

    def _apply_items(self, order: Order, items: list | None) -> None:
        assert items is not None
        mapped = [
            OrderItem(
                product_name=item.product_name,
                sku=item.sku,
                quantity=item.quantity,
            )
            for item in items
        ]
        order.replace_items(mapped)

    def _to_entity(self, request: OrderRequest) -> Order:
        return Order(
            id=request.id,
            customer_name=request.customer_name,
            status=request.status,
            pay_method=request.pay_method,
            user_grade=request.user_grade,
        )

    def _to_response(self, order: Order) -> OrderResponse:
        return OrderResponse(
            id=order.id,
            customer_name=order.customer_name,
            status=order.status,
            status_description=order.status.description if order.status else None,
            pay_method=order.pay_method,
            pay_method_description=order.pay_method.description if order.pay_method else None,
            user_grade=order.user_grade,
            user_grade_description=order.user_grade.description if order.user_grade else None,
            created_at=order.created_at,
            items=[
                OrderItemResponse(
                    id=item.id,
                    product_name=item.product_name,
                    sku=item.sku,
                    quantity=item.quantity,
                )
                for item in order.items
            ],
        )
