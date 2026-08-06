from __future__ import annotations

from datetime import datetime

from app.domain.enums import OrderStatus, PayMethod
from app.domain.flexible_search import OrderFlexibleSearchCriteriaBuilder
from app.web.schemas import OrderFlexibleSearchRequest


class TestOrderFlexibleSearchCriteriaBuilder:
    def test_empty_request_builds_empty_criteria(self) -> None:
        criteria = OrderFlexibleSearchCriteriaBuilder.from_request(
            OrderFlexibleSearchRequest()
        )
        assert criteria.is_empty() is True

    def test_blank_strings_and_nulls_are_ignored(self) -> None:
        request = OrderFlexibleSearchRequest(
            customerName="  ",
            orderDate="",
            fromDate=None,
            minQuantity=None,
        )
        assert OrderFlexibleSearchCriteriaBuilder.from_request(request).is_empty() is True

    def test_order_date_wins_over_between_fields(self) -> None:
        request = OrderFlexibleSearchRequest(
            orderDate="2026-08-06",
            fromDate="2026-01-01",
            toDate="2026-12-31",
        )
        criteria = OrderFlexibleSearchCriteriaBuilder.from_request(request)
        assert criteria.created_from == datetime(2026, 8, 6, 0, 0)
        assert criteria.created_to == datetime(2026, 8, 7, 0, 0)
        assert criteria.created_to_inclusive is False

    def test_maps_enum_number_and_string(self) -> None:
        request = OrderFlexibleSearchRequest(
            customerName=" Kim ",
            status=OrderStatus.PENDING,
            payMethod=PayMethod.CARD,
            minQuantity=3,
        )
        criteria = OrderFlexibleSearchCriteriaBuilder.from_request(request)
        assert criteria.customer_name == "Kim"
        assert criteria.status is OrderStatus.PENDING
        assert criteria.pay_method is PayMethod.CARD
        assert criteria.min_quantity == 3
