package com.example.enumapp.web;

import com.example.enumapp.domain.order.OrderService;
import com.example.enumapp.web.dto.OrderDateSearchRequest;
import com.example.enumapp.web.dto.OrderIdsRequest;
import com.example.enumapp.web.dto.OrderRequest;
import com.example.enumapp.web.dto.OrderResponse;
import com.example.enumapp.web.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 기본 CRUD(GET/PUT/PATCH/DELETE, POST 목록)는 클래스 설명만으로 충분하고,
 * 추가 path·행동 API 에만 {@link Operation} 을 둔다.
 */
@Tag(
        name = "Order Resource (JPA)",
        description = """
                단건: GET/PUT(생성)/PATCH/DELETE — id 기준.
                다건: POST(목록), 추가 path 로 다건 수정·삭제.
                행동: POST /{id}/cancel 등.
                """
)
@RestController
@RequestMapping("/api/resource/orders")
@Validated
public class OrderResourceController {

    private final OrderService orderService;

    public OrderResourceController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** POST 기본 목록 — 관례상 클래스 설명으로 충분 */
    @PostMapping
    public List<OrderResponse> list(@RequestBody(required = false) @Valid OrderDateSearchRequest request) {
        return orderService.list(request != null ? request : new OrderDateSearchRequest());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Validated(ValidationGroups.Create.class) @RequestBody @Valid OrderRequest request
    ) {
        return orderService.create(request);
    }

    @PatchMapping("/{id}")
    public OrderResponse update(
            @PathVariable Long id,
            @Validated(ValidationGroups.Update.class) @RequestBody @Valid OrderRequest request
    ) {
        request.setId(id);
        return orderService.update(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }

    @Operation(summary = "주문 취소", description = "단건 행동: POST /{id}/cancel — 상태를 CANCELLED 로 변경한다.")
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @Operation(summary = "다건 상태 수정", description = "ids + status 로 여러 주문의 상태를 일괄 변경한다.")
    @PostMapping("/update")
    public List<OrderResponse> updateMany(@RequestBody @Valid OrderIdsRequest request) {
        return orderService.updateStatusMany(request.getIds(), request.getStatus());
    }

    @Operation(summary = "다건 삭제", description = "ids 목록의 주문을 일괄 삭제한다.")
    @PostMapping("/delete")
    public void deleteMany(@RequestBody @Valid OrderIdsRequest request) {
        orderService.deleteMany(request.getIds());
    }
}
