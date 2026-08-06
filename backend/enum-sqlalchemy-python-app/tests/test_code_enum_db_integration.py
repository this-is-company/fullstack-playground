from __future__ import annotations

from datetime import datetime

from app.domain.enums import OrderStatus, PayMethod, UserGrade
from app.domain.models import Order, OrderItem
from app.domain.order_query import OrderQuery


class TestCodeEnumDbIntegration:
    def test_persists_and_loads_enums_by_code_and_allows_null_enums(self, db) -> None:
        order = Order(
            customer_name="null-enum-customer",
            status=None,
            pay_method=None,
            user_grade=UserGrade.BASIC,
            created_at=datetime.now(),
        )
        order.add_item(
            OrderItem(product_name="Pen", sku="SKU-1", quantity=1)
        )
        db.add(order)
        db.flush()

        loaded = OrderQuery(db).find_with_items_by_id(order.id)
        assert loaded is not None
        assert loaded.status is None
        assert loaded.pay_method is None
        assert loaded.user_grade is UserGrade.BASIC

        loaded.status = OrderStatus.PAID
        loaded.pay_method = PayMethod.CARD
        db.flush()

        updated = OrderQuery(db).find_with_items_by_id(order.id)
        assert updated is not None
        assert updated.status is OrderStatus.PAID
        assert updated.pay_method is PayMethod.CARD
        assert updated.status.code == "A"
