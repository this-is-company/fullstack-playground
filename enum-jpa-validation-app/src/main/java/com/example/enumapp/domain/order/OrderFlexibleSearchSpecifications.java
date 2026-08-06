package com.example.enumapp.domain.order;

import com.example.enumapp.common.jpa.ConditionBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class OrderFlexibleSearchSpecifications {

    private OrderFlexibleSearchSpecifications() {
    }

    public static Specification<Order> from(OrderFlexibleSearchCriteria criteria) {
        return (root, query, cb) -> {
            var condition = ConditionBuilder.create(cb);

            Join<Order, OrderItem> items = null;
            if (criteria.getMinQuantity() != null) {
                items = root.join("items", JoinType.LEFT);
                query.distinct(true);
            }

            return condition.and(
                    condition.eq(root.get("status"), criteria.getStatus()),
                    condition.like(root.get("customerName"), criteria.getCustomerName()),
                    condition.eq(root.get("payMethod"), criteria.getPayMethod()),
                    condition.range(
                            root.get("createdAt"),
                            criteria.getCreatedFrom(),
                            criteria.getCreatedTo(),
                            criteria.isCreatedToInclusive()
                    ),
                    items == null ? null : condition.goe(items.get("quantity"), criteria.getMinQuantity())
            );
        };
    }
}
