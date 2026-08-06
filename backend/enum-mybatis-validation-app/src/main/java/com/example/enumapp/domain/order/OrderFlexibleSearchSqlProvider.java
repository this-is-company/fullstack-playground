package com.example.enumapp.domain.order;

import com.example.enumapp.common.mybatis.ConditionBuilder;

/**
 * 유연 검색 SQL Provider — {@link ConditionBuilder} 로 where 조각을 만든다.
 */
public final class OrderFlexibleSearchSqlProvider {

    private OrderFlexibleSearchSqlProvider() {
    }

    public static String searchFlexible(OrderFlexibleSearchCriteria criteria) {
        var condition = ConditionBuilder.create()
                .eq("o.status", "status", criteria.getStatus())
                .like("o.customer_name", "customerName", criteria.getCustomerName())
                .eq("o.pay_method", "payMethod", criteria.getPayMethod())
                .range(
                        "o.created_at",
                        "createdFrom",
                        "createdTo",
                        criteria.getCreatedFrom(),
                        criteria.getCreatedTo(),
                        criteria.isCreatedToInclusive()
                )
                .goe("i.quantity", "minQuantity", criteria.getMinQuantity());

        return """
                <script>
                SELECT DISTINCT o.id, o.customer_name, o.status, o.pay_method, o.user_grade, o.created_at
                FROM demo_orders o
                LEFT JOIN demo_order_items i ON i.order_id = o.id
                %s
                ORDER BY o.id
                </script>
                """.formatted(condition.toWhereXml());
    }
}
