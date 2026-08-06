package com.example.enumapp.domain.order

import com.example.enumapp.common.querydsl.ConditionBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * POST 목록/검색 조회는 QueryDSL 로 동적 조건을 구성한다.
 */
@Repository
class OrderQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val condition = ConditionBuilder.create()

    fun findAllWithItems(): List<Order> {
        val order = QOrder.order
        val item = QOrderItem.orderItem
        return queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.items, item).fetchJoin()
            .orderBy(order.id.asc())
            .fetch()
    }

    fun findByIdsWithItems(ids: List<Long>?): List<Order> {
        if (ids.isNullOrEmpty()) {
            return emptyList()
        }
        val order = QOrder.order
        val item = QOrderItem.orderItem
        return queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.items, item).fetchJoin()
            .where(condition.`in`(order.id, ids))
            .orderBy(order.id.asc())
            .fetch()
    }

    /**
     * @param toInclusive true 이면 createdAt <= to, false 이면 createdAt < to
     */
    fun searchByCreatedAt(
        from: LocalDateTime?,
        to: LocalDateTime?,
        toInclusive: Boolean,
        minQuantity: Int?
    ): List<Order> {
        val order = QOrder.order
        val item = QOrderItem.orderItem
        return queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.items, item).fetchJoin()
            .where(
                condition.range(order.createdAt, from, to, toInclusive),
                condition.goe(item.quantity, minQuantity)
            )
            .orderBy(order.id.asc())
            .fetch()
    }

    /** 유연 검색 — null/blank 조건은 ConditionBuilder 가 null 로 반환 */
    fun searchFlexible(criteria: OrderFlexibleSearchCriteria): List<Order> {
        val order = QOrder.order
        val item = QOrderItem.orderItem
        return queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.items, item).fetchJoin()
            .where(
                condition.eq(order.status, criteria.status),
                condition.like(order.customerName, criteria.customerName),
                condition.eq(order.payMethod, criteria.payMethod),
                condition.range(
                    order.createdAt,
                    criteria.createdFrom,
                    criteria.createdTo,
                    criteria.createdToInclusive
                ),
                condition.goe(item.quantity, criteria.minQuantity)
            )
            .orderBy(order.id.asc())
            .fetch()
    }
}
