package com.example.enumapp.web.dto

import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.domain.order.UserGrade
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "주문 응답. enum 필드는 code, *Description 은 한글 설명.")
class OrderResponse {
    var id: Long? = null
    var customerName: String? = null

    @Schema(description = "주문 상태 code", example = "P")
    var status: OrderStatus? = null
        set(value) {
            field = value
            statusDescription = value?.description
        }

    @Schema(description = "주문 상태 설명 (예: 대기)")
    var statusDescription: String? = null
        private set

    @Schema(description = "결제수단 code", example = "CARD")
    var payMethod: PayMethod? = null
        set(value) {
            field = value
            payMethodDescription = value?.description
        }

    @Schema(description = "결제수단 설명")
    var payMethodDescription: String? = null
        private set

    @Schema(description = "회원 등급 code", example = "G")
    var userGrade: UserGrade? = null
        set(value) {
            field = value
            userGradeDescription = value?.description
        }

    @Schema(description = "회원 등급 설명")
    var userGradeDescription: String? = null
        private set

    var createdAt: LocalDateTime? = null
    var items: List<OrderItemResponse>? = null

    class OrderItemResponse {
        var id: Long? = null
        var productName: String? = null
        var sku: String? = null
        var quantity: Int? = null
    }
}
