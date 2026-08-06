package com.example.enumapp.web;

import com.example.enumapp.domain.order.OrderService;
import com.example.enumapp.web.dto.OrderDateSearchRequest;
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest;
import com.example.enumapp.web.dto.OrderRequest;
import com.example.enumapp.web.dto.OrderResponse;
import com.example.enumapp.web.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Order", description = "JPA 주문 API. POST 생성, PUT 수정, GET 단건. 검색은 POST /search/*.")
@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Validated(ValidationGroups.Create.class) @RequestBody @Valid OrderRequest request
    ) {
        return orderService.create(request);
    }

    @PutMapping("/{id}")
    public OrderResponse update(
            @PathVariable Long id,
            @Validated(ValidationGroups.Update.class) @RequestBody @Valid OrderRequest request
    ) {
        request.setId(id);
        return orderService.update(request);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @Operation(summary = "유연 검색", description = "날짜/between/숫자/enum/문자열 조건을 모두 optional 로 받는다. 전부 비어 있으면 전체 목록.")
    @PostMapping("/search")
    public List<OrderResponse> searchFlexible(@RequestBody(required = false) @Valid OrderFlexibleSearchRequest request) {
        return orderService.searchFlexible(request != null ? request : new OrderFlexibleSearchRequest());
    }

    @Operation(summary = "단일 일자 검색", description = "orderDate(yyyy-MM-dd) 기준 LocalDate 조회.")
    @PostMapping("/search/by-date")
    public List<OrderResponse> searchByDate(@RequestBody @Valid OrderDateSearchRequest request) {
        return orderService.searchBySingleDate(request);
    }

    @Operation(summary = "일자 between 검색", description = "fromDate~toDate(yyyy-MM-dd) LocalDate between.")
    @PostMapping("/search/by-date-between")
    public List<OrderResponse> searchByDateBetween(@RequestBody @Valid OrderDateSearchRequest request) {
        return orderService.searchByDateBetween(request);
    }

    @Operation(summary = "일시 between 검색", description = "fromDateTime~toDateTime LocalDateTime between.")
    @PostMapping("/search/by-datetime-between")
    public List<OrderResponse> searchByDateTimeBetween(@RequestBody @Valid OrderDateSearchRequest request) {
        return orderService.searchByDateTimeBetween(request);
    }
}