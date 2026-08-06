package com.example.enumapp.web

import com.example.enumapp.common.time.DateStrings
import com.example.enumapp.web.dto.GetValidationParsedResponse
import com.example.enumapp.web.dto.GetValidationQuery
import com.example.enumapp.web.validation.NotEmptyList
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * GET 쿼리 파라미터 검증 데모.
 *
 * 방법 A: 문자열 날짜 + `@LocalDateString` 검증 → [DateStrings] 로 변환
 * 방법 B: [LocalDate]/[LocalDateTime] 파라미터 + `@DateTimeFormat` (Spring 바인딩 변환)
 */
@Tag(name = "GET Validation Demo", description = "GET 리스트/중첩/숫자/날짜 검증 및 LocalDate 변환 확인")
@RestController
@RequestMapping("/api/demo/get-validation")
@Validated
class GetValidationDemoController {

    @Operation(
        summary = "문자열 날짜 검증 후 LocalDate/LocalDateTime 변환",
        description = """
            ids·items 리스트(null/empty/요소 null/내부 필드) + 숫자 + 날짜 문자열을 검증한다.
            통과 후 DateStrings 로 LocalDate/LocalDateTime 으로 바꾼 결과를 반환한다.
            예: ?ids=1&ids=2&items[0].sku=ABC&items[0].quantity=2&orderDate=2026-08-06&minQuantity=5
            """,
    )
    @GetMapping("/search")
    fun search(@Valid @ModelAttribute query: GetValidationQuery): GetValidationParsedResponse {
        return GetValidationParsedResponse().apply {
            ids = query.ids
            items = query.items!!.map { i ->
                GetValidationParsedResponse.ItemView(i.sku, i.quantity)
            }
            minQuantity = query.minQuantity
            page = query.page
            orderDate = DateStrings.toLocalDate(query.orderDate)
            fromDate = DateStrings.toLocalDate(query.fromDate)
            toDate = DateStrings.toLocalDate(query.toDate)
            fromDateTime = DateStrings.toLocalDateTime(query.fromDateTime)
            toDateTime = DateStrings.toLocalDateTime(query.toDateTime)
            note = "validated as strings then converted via DateStrings"
        }
    }

    @Operation(
        summary = "Spring @DateTimeFormat 으로 바로 LocalDate/LocalDateTime 바인딩",
        description = """
            쿼리 문자열을 Spring 이 LocalDate/LocalDateTime 으로 변환한다.
            형식이 틀리면 바인딩 오류(400). null/생략은 허용.
            예: ?orderDate=2026-08-06&fromDateTime=2026-08-06T09:00:00&minQuantity=3
            """,
    )
    @GetMapping("/typed-dates")
    fun typedDates(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        orderDate: LocalDate?,

        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        fromDateTime: LocalDateTime?,

        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        toDateTime: LocalDateTime?,

        @RequestParam(required = false)
        @Min(0)
        minQuantity: Int?,
    ): Map<String, Any?> {
        val body = LinkedHashMap<String, Any?>()
        body["orderDate"] = orderDate
        body["orderDateType"] = orderDate?.javaClass?.simpleName
        body["fromDateTime"] = fromDateTime
        body["fromDateTimeType"] = fromDateTime?.javaClass?.simpleName
        body["toDateTime"] = toDateTime
        body["minQuantity"] = minQuantity
        body["note"] = "bound directly to LocalDate/LocalDateTime by Spring"
        return body
    }

    @Operation(
        summary = "필수 ids 리스트 GET 검증",
        description = "ids 가 없거나 비면 400. 요소는 Long 으로 바인딩.",
    )
    @GetMapping("/required-ids")
    fun requiredIds(
        @RequestParam
        @NotEmptyList
        ids: List<@NotNull Long>,
    ): Map<String, Any> = mapOf(
        "ids" to ids,
        "note" to "List<@NotNull Long> + custom @NotEmptyList",
    )
}
