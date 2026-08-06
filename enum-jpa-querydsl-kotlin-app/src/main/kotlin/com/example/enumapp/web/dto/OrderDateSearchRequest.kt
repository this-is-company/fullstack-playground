package com.example.enumapp.web.dto

import com.example.enumapp.web.validation.LocalDateString
import com.example.enumapp.web.validation.LocalDateTimeString
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 날짜는 문자열로 받고, 서비스에서 LocalDate / LocalDateTime 으로 변환한다.
 * minQuantity 는 Wrapper(Integer) 라서 JSON null 이 0 으로 바뀌지 않아야 한다.
 */
@Schema(description = "주문 날짜/수량 검색 조건. 사용하는 조회 API에 맞는 필드만 채운다.")
class OrderDateSearchRequest {
    @Schema(description = "단일 일자 조회. yyyy-MM-dd → LocalDate", example = "2026-08-06")
    @field:LocalDateString
    var orderDate: String? = null

    @Schema(description = "between 시작일. yyyy-MM-dd → LocalDate", example = "2026-08-01")
    @field:LocalDateString
    var fromDate: String? = null

    @Schema(description = "between 종료일. yyyy-MM-dd → LocalDate", example = "2026-08-03")
    @field:LocalDateString
    var toDate: String? = null

    @Schema(description = "between 시작일시. yyyy-MM-dd'T'HH:mm:ss → LocalDateTime", example = "2026-08-06T09:00:00")
    @field:LocalDateTimeString
    var fromDateTime: String? = null

    @Schema(description = "between 종료일시. yyyy-MM-dd'T'HH:mm:ss → LocalDateTime", example = "2026-08-06T18:00:00")
    @field:LocalDateTimeString
    var toDateTime: String? = null

    @Schema(description = "최소 수량 필터. null 이면 필터 미적용(0 으로 바뀌지 않음)", nullable = true, example = "5")
    var minQuantity: Int? = null
}
