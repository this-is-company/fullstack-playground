from __future__ import annotations

from datetime import datetime

from sqlalchemy import select
from sqlalchemy.orm import Session, selectinload

from app.common.condition_builder import ConditionBuilder
from app.domain.flexible_search import OrderFlexibleSearchCriteria
from app.domain.models import Order, OrderItem


class OrderQuery:
    def __init__(self, db: Session) -> None:
        self.db = db
        self.condition = ConditionBuilder.create()

    def find_all_with_items(self) -> list[Order]:
        stmt = (
            select(Order)
            .options(selectinload(Order.items))
            .order_by(Order.id.asc())
        )
        return list(self.db.scalars(stmt).unique().all())

    def find_by_ids_with_items(self, ids: list[int] | None) -> list[Order]:
        if not ids:
            return []
        where = ConditionBuilder.and_combine(self.condition.in_(Order.id, ids))
        stmt = (
            select(Order)
            .options(selectinload(Order.items))
            .where(where)  # type: ignore[arg-type]
            .order_by(Order.id.asc())
        )
        return list(self.db.scalars(stmt).unique().all())

    def find_with_items_by_id(self, order_id: int) -> Order | None:
        stmt = (
            select(Order)
            .options(selectinload(Order.items))
            .where(Order.id == order_id)
        )
        return self.db.scalars(stmt).first()

    def search_by_created_at(
        self,
        from_: datetime | None,
        to: datetime | None,
        to_inclusive: bool,
        min_quantity: int | None,
    ) -> list[Order]:
        where = ConditionBuilder.and_combine(
            self.condition.range(Order.created_at, from_, to, to_inclusive),
            self.condition.goe(OrderItem.quantity, min_quantity),
        )
        stmt = (
            select(Order)
            .distinct()
            .outerjoin(Order.items)
            .options(selectinload(Order.items))
        )
        if where is not None:
            stmt = stmt.where(where)
        stmt = stmt.order_by(Order.id.asc())
        return list(self.db.scalars(stmt).unique().all())

    def search_flexible(self, criteria: OrderFlexibleSearchCriteria) -> list[Order]:
        where = ConditionBuilder.and_combine(
            self.condition.eq(Order.status, criteria.status),
            self.condition.like(Order.customer_name, criteria.customer_name),
            self.condition.eq(Order.pay_method, criteria.pay_method),
            self.condition.range(
                Order.created_at,
                criteria.created_from,
                criteria.created_to,
                criteria.created_to_inclusive,
            ),
            self.condition.goe(OrderItem.quantity, criteria.min_quantity),
        )
        stmt = (
            select(Order)
            .distinct()
            .outerjoin(Order.items)
            .options(selectinload(Order.items))
        )
        if where is not None:
            stmt = stmt.where(where)
        stmt = stmt.order_by(Order.id.asc())
        return list(self.db.scalars(stmt).unique().all())
