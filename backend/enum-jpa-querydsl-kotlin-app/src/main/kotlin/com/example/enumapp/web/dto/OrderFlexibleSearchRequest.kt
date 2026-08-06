package com.example.enumapp.web.dto

import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.web.validation.LocalDateString
import com.example.enumapp.web.validation.LocalDateTimeString
import com.example.enumapp.web.validation.NoSpecialChars
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 모든 필드가 비어 있어도 된다. 값이 있는 조건만 AND 로 적용한다.
 */
@Schema(description = "유연 검색. 날짜/between/숫자/enum/문자열 — 전부 optional.")
class OrderFlexibleSearchRequest {
    @Schema(description = "고객명 부분 일치", nullable = true, example = "Kim")
    @field:NoSpecialChars
    var customerName: String? = null

    @Schema(description = "주문 상태 code", nullable = true, example = "P")
    var status: OrderStatus? = null

    @Schema(description = "결제수단 code", nullable = true, example = "CARD")
    var payMethod: PayMethod? = null

    @Schema(description = "최소 수량. null 이면 미적용", nullable = true, example = "2")
    var minQuantity: Int? = null

    @Schema(description = "단일 일자. 있으면 해당일 00:00~다음날 00:00", example = "2026-08-06")
    @field:LocalDateString
    var orderDate: String? = null

    @Schema(description = "between 시작일", example = "2026-08-01")
    @field:LocalDateString
    var fromDate: String? = null

    @Schema(description = "between 종료일(포함, 다음날 00:00 미만)", example = "2026-08-03")
    @field:LocalDateString
    var toDate: String? = null

    @Schema(description = "between 시작일시", example = "2026-08-06T09:00:00")
    @field:LocalDateTimeString
    var fromDateTime: String? = null

    @Schema(description = "between 종료일시(포함)", example = "2026-08-06T18:00:00")
    @field:LocalDateTimeString
    var toDateTime: String? = null
}
