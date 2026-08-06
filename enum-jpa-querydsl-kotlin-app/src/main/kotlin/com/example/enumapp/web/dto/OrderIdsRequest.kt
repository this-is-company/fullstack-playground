package com.example.enumapp.web.dto

import com.example.enumapp.domain.order.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(description = "다건 수정/삭제 요청")
class OrderIdsRequest {
    @field:NotEmpty
    @Schema(description = "대상 주문 id 목록", example = "[1, 2, 3]")
    var ids: List<Long>? = null

    @Schema(description = "다건 수정(POST /update) 시 변경할 상태 코드. 삭제(POST /delete) 시에는 무시", example = "A", nullable = true)
    var status: OrderStatus? = null
}
