package com.example.enumapp.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "GET 검증 통과 후 파싱된 결과 (문자열 날짜 → LocalDate/LocalDateTime)")
class GetValidationParsedResponse {
    var ids: List<Long>? = null
    var items: List<ItemView>? = null
    var minQuantity: Int? = null
    var page: Int? = null
    var orderDate: LocalDate? = null
    var fromDate: LocalDate? = null
    var toDate: LocalDate? = null
    var fromDateTime: LocalDateTime? = null
    var toDateTime: LocalDateTime? = null
    var note: String? = null

    class ItemView(
        var sku: String? = null,
        var quantity: Int? = null,
    )
}
