package com.example.enumapp.domain.order

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

/**
 * 단건 조회/저장용. POST 검색·목록은 [OrderQueryRepository](QueryDSL).
 */
interface OrderRepository : JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = ["items"])
    @Query("select o from Order o where o.id = :id")
    fun findWithItemsById(@Param("id") id: Long): Optional<Order>
}
