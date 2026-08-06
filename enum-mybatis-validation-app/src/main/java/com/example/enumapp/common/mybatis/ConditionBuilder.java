package com.example.enumapp.common.mybatis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MyBatis 동적 WHERE 용 null-safe 조건 빌더 (QueryDSL/JPA ConditionBuilder 와 동일 역할).
 * 값이 없으면 조각을 추가하지 않는다.
 *
 * <pre>
 * var condition = ConditionBuilder.create();
 * String where = condition
 *     .eq("o.status", "status", status)
 *     .like("o.customer_name", "customerName", name)
 *     .range("o.created_at", "createdFrom", "createdTo", from, to, false)
 *     .in("o.id", "ids", ids)
 *     .goe("i.quantity", "minQuantity", minQuantity)
 *     .toWhereXml();
 * </pre>
 */
public final class ConditionBuilder {

    private final List<String> fragments = new ArrayList<>();

    private ConditionBuilder() {
    }

    public static ConditionBuilder create() {
        return new ConditionBuilder();
    }

    public ConditionBuilder eq(String column, String property, Object value) {
        if (value != null) {
            fragments.add(column + " = #{" + property + "}");
        }
        return this;
    }

    /** LOWER(column) LIKE %value% */
    public ConditionBuilder like(String column, String property, String value) {
        if (hasText(value)) {
            fragments.add("LOWER(" + column + ") LIKE CONCAT('%', LOWER(#{" + property + "}), '%')");
        }
        return this;
    }

    public ConditionBuilder between(String column, String fromProperty, String toProperty, Object from, Object to) {
        if (from != null && to != null) {
            fragments.add(column + " BETWEEN #{" + fromProperty + "} AND #{" + toProperty + "}");
        } else if (from != null) {
            fragments.add(column + " &gt;= #{" + fromProperty + "}");
        } else if (to != null) {
            fragments.add(column + " &lt;= #{" + toProperty + "}");
        }
        return this;
    }

    public ConditionBuilder range(
            String column,
            String fromProperty,
            String toProperty,
            Object from,
            Object to,
            boolean toInclusive
    ) {
        if (from != null) {
            fragments.add(column + " &gt;= #{" + fromProperty + "}");
        }
        if (to != null) {
            fragments.add(column + (toInclusive ? " &lt;= " : " &lt; ") + "#{" + toProperty + "}");
        }
        return this;
    }

    public ConditionBuilder in(String column, String property, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            fragments.add(column + " IN <foreach collection=\"" + property
                    + "\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>");
        }
        return this;
    }

    public ConditionBuilder goe(String column, String property, Object value) {
        if (value != null) {
            fragments.add(column + " &gt;= #{" + property + "}");
        }
        return this;
    }

    public ConditionBuilder loe(String column, String property, Object value) {
        if (value != null) {
            fragments.add(column + " &lt;= #{" + property + "}");
        }
        return this;
    }

    /** {@code <where>...</where>} 조각. 조건 없으면 빈 문자열 */
    public String toWhereXml() {
        if (fragments.isEmpty()) {
            return "";
        }
        return fragments.stream().collect(Collectors.joining(" AND ", "<where>", "</where>"));
    }

    List<String> fragmentsForTest() {
        return List.copyOf(fragments);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
