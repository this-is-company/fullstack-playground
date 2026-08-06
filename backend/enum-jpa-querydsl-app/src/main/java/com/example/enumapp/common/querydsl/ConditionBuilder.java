package com.example.enumapp.common.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;

import java.util.Collection;

/**
 * QueryDSL {@code where(...)} 에 바로 넣을 수 있는 null-safe 조건 팩토리.
 * 값이 없으면 {@code null} 을 반환하며, QueryDSL 은 null predicate 를 무시한다.
 *
 * <pre>
 * var condition = ConditionBuilder.create();
 *
 * queryFactory.selectFrom(order)
 *     .leftJoin(order.items, item).fetchJoin()
 *     .where(
 *         condition.eq(order.status, criteria.getStatus()),
 *         condition.like(order.customerName, criteria.getCustomerName()),
 *         condition.range(order.createdAt, from, to, false),
 *         condition.in(order.id, ids),
 *         condition.goe(item.quantity, minQuantity)
 *     )
 *     .fetch();
 * </pre>
 */
public final class ConditionBuilder {

    private static final ConditionBuilder INSTANCE = new ConditionBuilder();

    private ConditionBuilder() {
    }

    public static ConditionBuilder create() {
        return INSTANCE;
    }

    public <T> Predicate eq(SimpleExpression<T> path, T value) {
        return value == null ? null : path.eq(value);
    }

    /** 부분 일치 (대소문자 무시) */
    public Predicate containsIgnoreCase(StringExpression path, String value) {
        return hasText(value) ? path.containsIgnoreCase(value.trim()) : null;
    }

    /** LIKE %value% */
    public Predicate like(StringExpression path, String value) {
        return hasText(value) ? path.like("%" + value.trim() + "%") : null;
    }

    /** between. 한쪽만 있으면 goe/loe */
    public <T extends Comparable<?>> Predicate between(ComparableExpression<T> path, T from, T to) {
        if (from != null && to != null) {
            return path.between(from, to);
        }
        if (from != null) {
            return path.goe(from);
        }
        if (to != null) {
            return path.loe(to);
        }
        return null;
    }

    public <T extends Number & Comparable<?>> Predicate between(NumberExpression<T> path, T from, T to) {
        if (from != null && to != null) {
            return path.between(from, to);
        }
        if (from != null) {
            return path.goe(from);
        }
        if (to != null) {
            return path.loe(to);
        }
        return null;
    }

    /** toInclusive=false 이면 to 는 exclusive (&lt;) */
    public <T extends Comparable<?>> Predicate range(
            ComparableExpression<T> path,
            T from,
            T to,
            boolean toInclusive
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        if (from != null) {
            builder.and(path.goe(from));
        }
        if (to != null) {
            builder.and(toInclusive ? path.loe(to) : path.lt(to));
        }
        return builder.hasValue() ? builder : null;
    }

    public <T extends Number & Comparable<?>> Predicate range(
            NumberExpression<T> path,
            T from,
            T to,
            boolean toInclusive
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        if (from != null) {
            builder.and(path.goe(from));
        }
        if (to != null) {
            builder.and(toInclusive ? path.loe(to) : path.lt(to));
        }
        return builder.hasValue() ? builder : null;
    }

    public <T> Predicate in(SimpleExpression<T> path, Collection<? extends T> values) {
        return (values == null || values.isEmpty()) ? null : path.in(values);
    }

    public <T extends Number & Comparable<?>> Predicate goe(NumberExpression<T> path, T value) {
        return value == null ? null : path.goe(value);
    }

    public <T extends Number & Comparable<?>> Predicate loe(NumberExpression<T> path, T value) {
        return value == null ? null : path.loe(value);
    }

    public <T extends Comparable<?>> Predicate goe(ComparableExpression<T> path, T value) {
        return value == null ? null : path.goe(value);
    }

    public <T extends Comparable<?>> Predicate loe(ComparableExpression<T> path, T value) {
        return value == null ? null : path.loe(value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
