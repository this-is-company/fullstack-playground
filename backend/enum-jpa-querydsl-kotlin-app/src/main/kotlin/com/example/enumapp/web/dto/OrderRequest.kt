package com.example.enumapp.web.dto

import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.domain.order.UserGrade
import com.example.enumapp.web.validation.NoSpecialChars
import com.example.enumapp.web.validation.NotBlankFieldsInList
import com.example.enumapp.web.validation.ValidationGroups
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Null
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

@Schema(description = "주문 생성/수정 요청. enum 은 code 문자열로 주고받는다 (예: status=P).")
class OrderRequest {
    @Schema(description = "생성 시 null. 수정 시 path 의 id 를 쓰므로 body 에 넣지 않아도 됨", nullable = true)
    @field:Null(groups = [ValidationGroups.Create::class], message = "id must be null on create")
    var id: Long? = null

    @field:NotBlank(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
    @field:NoSpecialChars(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
    var customerName: String? = null

    @Schema(description = "주문 상태 code. 생성 시 필수, 수정 시 null/blank 이면 기존 값 유지", example = "P", nullable = true)
    @field:NotNull(groups = [ValidationGroups.Create::class], message = "status is required on create")
    var status: OrderStatus? = null

    @Schema(description = "결제수단 code. 생성 시 필수, 수정 시 null/blank 이면 기존 값 유지", example = "CARD", nullable = true)
    @field:NotNull(groups = [ValidationGroups.Create::class], message = "payMethod is required on create")
    var payMethod: PayMethod? = null

    @Schema(description = "회원 등급 code", example = "G")
    @field:NotNull(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
    var userGrade: UserGrade? = null

    /**
     * @Pattern 단순 패턴 검증 예시 필드.
     * null 이면 검사하지 않음. 값이 있으면 영문/숫자/_/- 만 허용.
     */
    @Schema(description = "@Pattern 예시. 영문/숫자/_/- 만 허용", example = "PROMO_01", nullable = true)
    @field:Pattern(
        regexp = "^[A-Za-z0-9_-]+$",
        message = "promoCode must match pattern [A-Za-z0-9_-]+",
        groups = [ValidationGroups.Create::class, ValidationGroups.Update::class],
    )
    var promoCode: String? = null

    @Schema(description = "주문 상품 목록. null/empty 및 내부 blank 필드 불가")
    @field:NotBlankFieldsInList(
        fields = ["productName", "sku"],
        groups = [ValidationGroups.Create::class, ValidationGroups.Update::class],
    )
    @field:Valid
    var items: List<OrderItemRequest>? = null

    class OrderItemRequest {
        @field:NotBlank(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
        @field:NoSpecialChars(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
        var productName: String? = null

        @field:NotBlank(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
        @field:NoSpecialChars(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
        var sku: String? = null

        @field:NotNull(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
        @field:Positive(groups = [ValidationGroups.Create::class, ValidationGroups.Update::class])
        var quantity: Int? = null
    }
}
