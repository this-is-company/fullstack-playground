package com.example.enumapp.web.dto

import com.example.enumapp.web.validation.LocalDateString
import com.example.enumapp.web.validation.LocalDateTimeString
import com.example.enumapp.web.validation.NoNullElements
import com.example.enumapp.web.validation.NoSpecialChars
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * GET 쿼리용 검증 DTO.
 *
 * - ids — 리스트 null/empty + 요소 null 금지
 * - items — 중첩 객체 리스트, 내부 필드 null/blank/숫자 검증
 * - 날짜 — 문자열로 받아 형식 검증 후 LocalDate/LocalDateTime 변환
 * - 숫자 — Wrapper 로 null 유지, 값이 있으면 범위 검증
 *
 * 예: `GET ...?ids=1&ids=2&minQuantity=5&orderDate=2026-08-06
 * &items[0].sku=ABC&items[0].quantity=2`
 */
@Schema(description = "GET 쿼리 검증 데모 파라미터")
class GetValidationQuery {

    @field:NotEmpty(message = "ids must not be null or empty")
    @field:NoNullElements
    @Schema(
        description = "주문 id 목록. 반복 파라미터: ids=1&ids=2",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var ids: List<Long>? = null

    @field:Valid
    @field:NotEmpty(message = "items must not be null or empty")
    @field:NoNullElements
    @Schema(
        description = "중첩 필터. items[0].sku=..&items[0].quantity=..",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    var items: List<ItemFilter>? = null

    @field:Min(value = 0, message = "minQuantity must be >= 0")
    @Schema(description = "최소 수량. 생략(null) 가능, 있으면 0 이상", nullable = true, example = "5")
    var minQuantity: Int? = null

    @field:Positive(message = "page must be positive")
    @Schema(description = "페이지(선택). 있으면 양수", nullable = true, example = "1")
    var page: Int? = null

    @field:LocalDateString
    @Schema(description = "단일 일자 문자열 yyyy-MM-dd", example = "2026-08-06")
    var orderDate: String? = null

    @field:LocalDateString
    @Schema(description = "시작일 yyyy-MM-dd", example = "2026-08-01")
    var fromDate: String? = null

    @field:LocalDateString
    @Schema(description = "종료일 yyyy-MM-dd", example = "2026-08-03")
    var toDate: String? = null

    @field:LocalDateTimeString
    @Schema(description = "시작일시 yyyy-MM-dd'T'HH:mm:ss", example = "2026-08-06T09:00:00")
    var fromDateTime: String? = null

    @field:LocalDateTimeString
    @Schema(description = "종료일시 yyyy-MM-dd'T'HH:mm:ss", example = "2026-08-06T18:00:00")
    var toDateTime: String? = null

    class ItemFilter {
        @field:NotBlank(message = "sku must not be blank")
        @field:NoSpecialChars
        @Schema(example = "SKU-001")
        var sku: String? = null

        @field:NotNull(message = "quantity must not be null")
        @field:Positive(message = "quantity must be positive")
        @Schema(example = "2")
        var quantity: Int? = null
    }
}
