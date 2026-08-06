package com.example.enumapp.web

import com.example.enumapp.domain.order.OrderService
import com.example.enumapp.web.dto.OrderDateSearchRequest
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest
import com.example.enumapp.web.dto.OrderRequest
import com.example.enumapp.web.dto.OrderResponse
import com.example.enumapp.web.validation.ValidationGroups
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Order", description = "JPA+QueryDSL 주문 API. POST 생성, PUT 수정, GET 단건. 검색은 POST /search/*.")
@RestController
@RequestMapping("/api/orders")
@Validated
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Validated(ValidationGroups.Create::class) @RequestBody @Valid request: OrderRequest,
    ): OrderResponse = orderService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Validated(ValidationGroups.Update::class) @RequestBody @Valid request: OrderRequest,
    ): OrderResponse {
        request.id = id
        return orderService.update(request)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): OrderResponse = orderService.get(id)

    @Operation(summary = "유연 검색", description = "날짜/between/숫자/enum/문자열 조건을 모두 optional 로 받는다. 전부 비어 있으면 전체 목록. QueryDSL BooleanBuilder.")
    @PostMapping("/search")
    fun searchFlexible(
        @RequestBody(required = false) @Valid request: OrderFlexibleSearchRequest?,
    ): List<OrderResponse> =
        orderService.searchFlexible(request ?: OrderFlexibleSearchRequest())

    @Operation(summary = "단일 일자 검색", description = "orderDate(yyyy-MM-dd) 기준 LocalDate 조회.")
    @PostMapping("/search/by-date")
    fun searchByDate(@RequestBody @Valid request: OrderDateSearchRequest): List<OrderResponse> =
        orderService.searchBySingleDate(request)

    @Operation(summary = "일자 between 검색", description = "fromDate~toDate(yyyy-MM-dd) LocalDate between.")
    @PostMapping("/search/by-date-between")
    fun searchByDateBetween(@RequestBody @Valid request: OrderDateSearchRequest): List<OrderResponse> =
        orderService.searchByDateBetween(request)

    @Operation(summary = "일시 between 검색", description = "fromDateTime~toDateTime LocalDateTime between.")
    @PostMapping("/search/by-datetime-between")
    fun searchByDateTimeBetween(@RequestBody @Valid request: OrderDateSearchRequest): List<OrderResponse> =
        orderService.searchByDateTimeBetween(request)
}
