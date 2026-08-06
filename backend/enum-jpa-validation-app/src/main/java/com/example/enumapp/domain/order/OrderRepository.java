package com.example.enumapp.domain.order;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);

    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o order by o.id")
    List<Order> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "items")
    @Query("select o from Order o where o.id in :ids order by o.id")
    List<Order> findByIdInOrderByIdAsc(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = "items")
    List<Order> findAll(Specification<Order> spec, Sort sort);

    @EntityGraph(attributePaths = "items")
    @Query("""
            select distinct o from Order o
            left join o.items i
            where o.createdAt >= :from
              and o.createdAt < :to
              and (:minQuantity is null or i.quantity >= :minQuantity)
            order by o.id
            """)
    List<Order> searchByCreatedAtRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("minQuantity") Integer minQuantity
    );

    @EntityGraph(attributePaths = "items")
    @Query("""
            select distinct o from Order o
            left join o.items i
            where o.createdAt >= :from
              and o.createdAt <= :to
              and (:minQuantity is null or i.quantity >= :minQuantity)
            order by o.id
            """)
    List<Order> searchByCreatedAtDateTimeBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("minQuantity") Integer minQuantity
    );
}
