package com.example.enumapp.domain.order;

import com.example.enumapp.common.querydsl.ConditionBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * POST 목록/검색 조회는 QueryDSL 로 동적 조건을 구성한다.
 */
@Repository
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ConditionBuilder condition = ConditionBuilder.create();

    public OrderQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Order> findAllWithItems() {
        QOrder order = QOrder.order;
        QOrderItem item = QOrderItem.orderItem;
        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.items, item).fetchJoin()
                .orderBy(order.id.asc())
                .fetch();
    }

    public List<Order> findByIdsWithItems(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        QOrder order = QOrder.order;
        QOrderItem item = QOrderItem.orderItem;
        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.items, item).fetchJoin()
                .where(condition.in(order.id, ids))
                .orderBy(order.id.asc())
                .fetch();
    }

    /**
     * @param toInclusive true 이면 createdAt &lt;= to, false 이면 createdAt &lt; to
     */
    public List<Order> searchByCreatedAt(
            LocalDateTime from,
            LocalDateTime to,
            boolean toInclusive,
            Integer minQuantity
    ) {
        QOrder order = QOrder.order;
        QOrderItem item = QOrderItem.orderItem;
        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.items, item).fetchJoin()
                .where(
                        condition.range(order.createdAt, from, to, toInclusive),
                        condition.goe(item.quantity, minQuantity)
                )
                .orderBy(order.id.asc())
                .fetch();
    }

    /** 유연 검색 — null/blank 조건은 ConditionBuilder 가 null 로 반환 */
    public List<Order> searchFlexible(OrderFlexibleSearchCriteria criteria) {
        QOrder order = QOrder.order;
        QOrderItem item = QOrderItem.orderItem;
        return queryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.items, item).fetchJoin()
                .where(
                        condition.eq(order.status, criteria.getStatus()),
                        condition.like(order.customerName, criteria.getCustomerName()),
                        condition.eq(order.payMethod, criteria.getPayMethod()),
                        condition.range(
                                order.createdAt,
                                criteria.getCreatedFrom(),
                                criteria.getCreatedTo(),
                                criteria.isCreatedToInclusive()
                        ),
                        condition.goe(item.quantity, criteria.getMinQuantity())
                )
                .orderBy(order.id.asc())
                .fetch();
    }
}
