package com.example.enumapp.domain.order

import com.example.enumapp.common.time.DateStrings
import com.example.enumapp.web.ApiMessageCodes
import com.example.enumapp.web.BusinessException
import com.example.enumapp.web.dto.OrderDateSearchRequest
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest
import com.example.enumapp.web.dto.OrderRequest
import com.example.enumapp.web.dto.OrderResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderQueryRepository: OrderQueryRepository
) {

    @Transactional
    fun create(request: OrderRequest): OrderResponse {
        val order = toEntity(request)
        if (order.createdAt == null) {
            order.createdAt = LocalDateTime.now()
        }
        applyItems(order, request.items)
        orderRepository.save(order)
        return get(order.id!!)
    }

    @Transactional
    fun createWithCreatedAt(request: OrderRequest, createdAt: LocalDateTime): OrderResponse {
        val order = toEntity(request)
        order.createdAt = createdAt
        applyItems(order, request.items)
        orderRepository.save(order)
        return get(order.id!!)
    }

    @Transactional
    fun update(request: OrderRequest): OrderResponse {
        val existing = orderRepository.findWithItemsById(request.id!!)
            .orElseThrow {
                BusinessException.notFound(
                    ApiMessageCodes.ORDER_NOT_FOUND,
                    "order not found: ${request.id}"
                )
            }

        existing.customerName = request.customerName
        existing.userGrade = request.userGrade
        if (request.status != null) {
            existing.status = request.status
        }
        if (request.payMethod != null) {
            existing.payMethod = request.payMethod
        }
        applyItems(existing, request.items)
        return get(existing.id!!)
    }

    @Transactional(readOnly = true)
    fun get(id: Long): OrderResponse {
        val order = orderRepository.findWithItemsById(id)
            .orElseThrow {
                BusinessException.notFound(
                    ApiMessageCodes.ORDER_NOT_FOUND,
                    "order not found: $id"
                )
            }
        return toResponse(order)
    }

    @Transactional
    fun delete(id: Long) {
        if (!orderRepository.existsById(id)) {
            throw BusinessException.notFound(ApiMessageCodes.ORDER_NOT_FOUND, "order not found: $id")
        }
        orderRepository.deleteById(id)
    }

    @Transactional
    fun cancel(id: Long): OrderResponse {
        val existing = orderRepository.findById(id)
            .orElseThrow {
                BusinessException.notFound(
                    ApiMessageCodes.ORDER_NOT_FOUND,
                    "order not found: $id"
                )
            }
        if (existing.status == OrderStatus.CANCELLED) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "order already cancelled: $id")
        }
        existing.status = OrderStatus.CANCELLED
        return get(id)
    }

    /** POST 목록 — 조건에 따라 QueryDSL 동적 조회 */
    @Transactional(readOnly = true)
    fun list(request: OrderDateSearchRequest): List<OrderResponse> {
        if (!request.orderDate.isNullOrBlank()) {
            return searchBySingleDate(request)
        }
        if (!request.fromDate.isNullOrBlank() && !request.toDate.isNullOrBlank()) {
            return searchByDateBetween(request)
        }
        if (!request.fromDateTime.isNullOrBlank() && !request.toDateTime.isNullOrBlank()) {
            return searchByDateTimeBetween(request)
        }
        return mapList(orderQueryRepository.findAllWithItems())
    }

    /**
     * 유연 검색: Criteria Builder → QueryDSL BooleanBuilder.
     * 모든 조건이 비어 있으면 전체 목록.
     */
    @Transactional(readOnly = true)
    fun searchFlexible(request: OrderFlexibleSearchRequest): List<OrderResponse> {
        val criteria = OrderFlexibleSearchCriteriaBuilder.from(request)
        return mapList(orderQueryRepository.searchFlexible(criteria))
    }

    @Transactional
    fun updateStatusMany(ids: List<Long>, status: OrderStatus?): List<OrderResponse> {
        if (status == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "status is required for bulk update")
        }
        for (id in ids) {
            val existing = orderRepository.findById(id)
                .orElseThrow {
                    BusinessException.notFound(
                        ApiMessageCodes.ORDER_NOT_FOUND,
                        "order not found: $id"
                    )
                }
            existing.status = status
        }
        return mapList(orderQueryRepository.findByIdsWithItems(ids))
    }

    @Transactional
    fun deleteMany(ids: List<Long>?) {
        if (ids.isNullOrEmpty()) {
            return
        }
        orderRepository.deleteAllById(ids)
    }

    @Transactional(readOnly = true)
    fun searchBySingleDate(request: OrderDateSearchRequest): List<OrderResponse> {
        val criteria = toCriteria(request)
        if (criteria.orderDate == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "orderDate is required")
        }
        val from = criteria.orderDate!!.atStartOfDay()
        val to = criteria.orderDate!!.plusDays(1).atStartOfDay()
        return mapList(orderQueryRepository.searchByCreatedAt(from, to, false, criteria.minQuantity))
    }

    @Transactional(readOnly = true)
    fun searchByDateBetween(request: OrderDateSearchRequest): List<OrderResponse> {
        val criteria = toCriteria(request)
        if (criteria.fromDate == null || criteria.toDate == null) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "fromDate and toDate are required")
        }
        val from = criteria.fromDate!!.atStartOfDay()
        val to = criteria.toDate!!.plusDays(1).atStartOfDay()
        return mapList(orderQueryRepository.searchByCreatedAt(from, to, false, criteria.minQuantity))
    }

    @Transactional(readOnly = true)
    fun searchByDateTimeBetween(request: OrderDateSearchRequest): List<OrderResponse> {
        val criteria = toCriteria(request)
        if (criteria.fromDateTime == null || criteria.toDateTime == null) {
            throw BusinessException.badRequest(
                ApiMessageCodes.BAD_REQUEST,
                "fromDateTime and toDateTime are required"
            )
        }
        return mapList(
            orderQueryRepository.searchByCreatedAt(
                criteria.fromDateTime,
                criteria.toDateTime,
                true,
                criteria.minQuantity
            )
        )
    }

    fun toCriteria(request: OrderDateSearchRequest): OrderSearchCriteria {
        val criteria = OrderSearchCriteria()
        criteria.orderDate = DateStrings.toLocalDate(request.orderDate)
        criteria.fromDate = DateStrings.toLocalDate(request.fromDate)
        criteria.toDate = DateStrings.toLocalDate(request.toDate)
        criteria.fromDateTime = DateStrings.toLocalDateTime(request.fromDateTime)
        criteria.toDateTime = DateStrings.toLocalDateTime(request.toDateTime)
        criteria.minQuantity = request.minQuantity
        return criteria
    }

    private fun mapList(orders: List<Order>): List<OrderResponse> =
        orders.map { toResponse(it) }

    private fun applyItems(order: Order, items: List<OrderRequest.OrderItemRequest>?) {
        val mapped = items!!.map { itemRequest ->
            OrderItem().apply {
                productName = itemRequest.productName
                sku = itemRequest.sku
                quantity = itemRequest.quantity
            }
        }
        order.replaceItems(mapped)
    }

    private fun toEntity(request: OrderRequest): Order =
        Order().apply {
            id = request.id
            customerName = request.customerName
            status = request.status
            payMethod = request.payMethod
            userGrade = request.userGrade
        }

    private fun toResponse(order: Order): OrderResponse {
        val response = OrderResponse()
        response.id = order.id
        response.customerName = order.customerName
        response.status = order.status
        response.payMethod = order.payMethod
        response.userGrade = order.userGrade
        response.createdAt = order.createdAt
        response.items = order.items.map { item ->
            OrderResponse.OrderItemResponse().apply {
                id = item.id
                productName = item.productName
                sku = item.sku
                quantity = item.quantity
            }
        }
        return response
    }
}
