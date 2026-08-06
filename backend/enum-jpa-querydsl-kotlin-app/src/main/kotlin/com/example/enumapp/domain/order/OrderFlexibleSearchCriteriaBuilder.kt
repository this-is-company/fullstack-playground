package com.example.enumapp.domain.order

import com.example.enumapp.common.time.DateStrings
import com.example.enumapp.web.dto.OrderFlexibleSearchRequest
import java.time.LocalDateTime

/**
 * Request(전부 optional) → typed [OrderFlexibleSearchCriteria].
 * 빈 문자열/null 은 조건에서 제외한다.
 */
object OrderFlexibleSearchCriteriaBuilder {

    @JvmStatic
    fun from(request: OrderFlexibleSearchRequest?): OrderFlexibleSearchCriteria {
        val builder = OrderFlexibleSearchCriteria.builder()
        if (request == null) {
            return builder.build()
        }

        if (hasText(request.customerName)) {
            builder.customerName(request.customerName!!.trim())
        }
        if (request.status != null) {
            builder.status(request.status)
        }
        if (request.payMethod != null) {
            builder.payMethod(request.payMethod)
        }
        if (request.minQuantity != null) {
            builder.minQuantity(request.minQuantity)
        }

        applyCreatedAtRange(builder, request)
        return builder.build()
    }

    /**
     * 우선순위: orderDate > fromDate/toDate > fromDateTime/toDateTime
     * (동시에 여러 날짜 그룹이 와도 하나만 적용)
     */
    private fun applyCreatedAtRange(
        builder: OrderFlexibleSearchCriteria.Builder,
        request: OrderFlexibleSearchRequest
    ) {
        if (hasText(request.orderDate)) {
            val day = DateStrings.toLocalDate(request.orderDate)!!
            builder.createdFrom(day.atStartOfDay())
            builder.createdTo(day.plusDays(1).atStartOfDay(), false)
            return
        }

        val hasDateBound = hasText(request.fromDate) || hasText(request.toDate)
        if (hasDateBound) {
            if (hasText(request.fromDate)) {
                builder.createdFrom(DateStrings.toLocalDate(request.fromDate)!!.atStartOfDay())
            }
            if (hasText(request.toDate)) {
                builder.createdTo(
                    DateStrings.toLocalDate(request.toDate)!!.plusDays(1).atStartOfDay(),
                    false
                )
            }
            return
        }

        if (hasText(request.fromDateTime) || hasText(request.toDateTime)) {
            if (hasText(request.fromDateTime)) {
                builder.createdFrom(DateStrings.toLocalDateTime(request.fromDateTime))
            }
            if (hasText(request.toDateTime)) {
                val to: LocalDateTime = DateStrings.toLocalDateTime(request.toDateTime)!!
                builder.createdTo(to, true)
            }
        }
    }

    private fun hasText(value: String?): Boolean =
        value != null && value.isNotBlank()
}
