package com.example.enumapp.web

import com.example.enumapp.domain.order.OrderService
import com.example.enumapp.web.dto.OrderDateSearchRequest
import com.example.enumapp.web.dto.OrderIdsRequest
import com.example.enumapp.web.dto.OrderRequest
import com.example.enumapp.web.dto.OrderResponse
import com.example.enumapp.web.validation.ValidationGroups
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 기본 CRUD(GET/PUT/PATCH/DELETE, POST 목록)는 클래스 설명만으로 충분하고,
 * 추가 path·행동 API 에만 [Operation] 을 둔다.
 */
@Tag(
    name = "Order Resource (QueryDSL)",
    description = """
                단건: GET/PUT(생성)/PATCH/DELETE — id 기준.
                다건: POST(목록), 추가 path 로 다건 수정·삭제.
                행동: POST /{id}/cancel 등.
                """,
)
@RestController
@RequestMapping("/api/resource/orders")
@Validated
class OrderResourceController(
    private val orderService: OrderService,
) {

    /** POST 기본 목록 — 관례상 클래스 설명으로 충분 */
    @PostMapping
    fun list(
        @RequestBody(required = false) @Valid request: OrderDateSearchRequest?,
    ): List<OrderResponse> =
        orderService.list(request ?: OrderDateSearchRequest())

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): OrderResponse = orderService.get(id)

    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Validated(ValidationGroups.Create::class) @RequestBody @Valid request: OrderRequest,
    ): OrderResponse = orderService.create(request)

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Validated(ValidationGroups.Update::class) @RequestBody @Valid request: OrderRequest,
    ): OrderResponse {
        request.id = id
        return orderService.update(request)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        orderService.delete(id)
    }

    @Operation(summary = "주문 취소", description = "단건 행동: POST /{id}/cancel — 상태를 CANCELLED 로 변경한다.")
    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): OrderResponse = orderService.cancel(id)

    @Operation(summary = "다건 상태 수정", description = "ids + status 로 여러 주문의 상태를 일괄 변경한다.")
    @PostMapping("/update")
    fun updateMany(@RequestBody @Valid request: OrderIdsRequest): List<OrderResponse> =
        orderService.updateStatusMany(request.ids!!, request.status)

    @Operation(summary = "다건 삭제", description = "ids 목록의 주문을 일괄 삭제한다.")
    @PostMapping("/delete")
    fun deleteMany(@RequestBody @Valid request: OrderIdsRequest) {
        orderService.deleteMany(request.ids!!)
    }
}
