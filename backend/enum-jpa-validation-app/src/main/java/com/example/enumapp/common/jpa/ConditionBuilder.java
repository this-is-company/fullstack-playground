package com.example.enumapp.common.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * JPA Criteria {@link Predicate} 용 null-safe 조건 팩토리 (QueryDSL ConditionBuilder 와 동일 역할).
 * 값이 없으면 {@code null} 을 반환한다.
 *
 * <pre>
 * var condition = ConditionBuilder.create(cb);
 * return condition.and(
 *     condition.eq(root.get("status"), status),
 *     condition.like(root.get("customerName"), name),
 *     condition.range(root.get("createdAt"), from, to, false),
 *     condition.in(root.get("id"), ids),
 *     condition.goe(items.get("quantity"), minQuantity)
 * );
 * </pre>
 */
public final class ConditionBuilder {

    private final CriteriaBuilder cb;

    private ConditionBuilder(CriteriaBuilder cb) {
        this.cb = cb;
    }

    public static ConditionBuilder create(CriteriaBuilder cb) {
        return new ConditionBuilder(cb);
    }

    public <T> Predicate eq(Expression<T> path, T value) {
        return value == null ? null : cb.equal(path, value);
    }

    public Predicate like(Expression<String> path, String value) {
        if (!hasText(value)) {
            return null;
        }
        return cb.like(cb.lower(path), "%" + value.trim().toLowerCase() + "%");
    }

    public <T extends Comparable<? super T>> Predicate between(Expression<T> path, T from, T to) {
        if (from != null && to != null) {
            return cb.between(path, from, to);
        }
        if (from != null) {
            return cb.greaterThanOrEqualTo(path, from);
        }
        if (to != null) {
            return cb.lessThanOrEqualTo(path, to);
        }
        return null;
    }

    public <T extends Comparable<? super T>> Predicate range(
            Expression<T> path,
            T from,
            T to,
            boolean toInclusive
    ) {
        Predicate fromPred = from == null ? null : cb.greaterThanOrEqualTo(path, from);
        Predicate toPred = null;
        if (to != null) {
            toPred = toInclusive ? cb.lessThanOrEqualTo(path, to) : cb.lessThan(path, to);
        }
        return and(fromPred, toPred);
    }

    public <T> Predicate in(Expression<T> path, Collection<? extends T> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return path.in(values);
    }

    public <T extends Comparable<? super T>> Predicate goe(Expression<T> path, T value) {
        return value == null ? null : cb.greaterThanOrEqualTo(path, value);
    }

    public <T extends Comparable<? super T>> Predicate loe(Expression<T> path, T value) {
        return value == null ? null : cb.lessThanOrEqualTo(path, value);
    }

    /** null predicate 는 무시하고 AND. 모두 null 이면 conjunction(true) */
    public Predicate and(Predicate... predicates) {
        Predicate[] present = Arrays.stream(predicates)
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);
        if (present.length == 0) {
            return cb.conjunction();
        }
        if (present.length == 1) {
            return present[0];
        }
        return cb.and(present);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
