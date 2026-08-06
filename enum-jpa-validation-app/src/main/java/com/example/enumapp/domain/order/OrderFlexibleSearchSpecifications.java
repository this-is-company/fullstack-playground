package com.example.enumapp.domain.order;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrderFlexibleSearchSpecifications {

    private OrderFlexibleSearchSpecifications() {
    }

    public static Specification<Order> from(OrderFlexibleSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getCustomerName() != null) {
                predicates.add(cb.like(
                        cb.lower(root.get("customerName")),
                        "%" + criteria.getCustomerName().toLowerCase() + "%"
                ));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getPayMethod() != null) {
                predicates.add(cb.equal(root.get("payMethod"), criteria.getPayMethod()));
            }
            if (criteria.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedFrom()));
            }
            if (criteria.getCreatedTo() != null) {
                if (criteria.isCreatedToInclusive()) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedTo()));
                } else {
                    predicates.add(cb.lessThan(root.get("createdAt"), criteria.getCreatedTo()));
                }
            }
            if (criteria.getMinQuantity() != null) {
                Join<Order, OrderItem> items = root.join("items", JoinType.LEFT);
                predicates.add(cb.greaterThanOrEqualTo(items.get("quantity"), criteria.getMinQuantity()));
                query.distinct(true);
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
