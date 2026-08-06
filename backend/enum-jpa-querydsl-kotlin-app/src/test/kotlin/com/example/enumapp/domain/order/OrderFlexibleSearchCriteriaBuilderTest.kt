package com.example.enumapp.domain.order

import com.example.enumapp.web.dto.OrderFlexibleSearchRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class OrderFlexibleSearchCriteriaBuilderTest {

    @Test
    fun emptyRequest_buildsEmptyCriteria() {
        val criteria = OrderFlexibleSearchCriteriaBuilder.from(OrderFlexibleSearchRequest())
        assertThat(criteria.isEmpty()).isTrue()
    }

    @Test
    fun blankStringsAndNulls_areIgnored() {
        val request = OrderFlexibleSearchRequest().apply {
            customerName = "  "
            orderDate = ""
            fromDate = null
            minQuantity = null
        }
        assertThat(OrderFlexibleSearchCriteriaBuilder.from(request).isEmpty()).isTrue()
    }

    @Test
    fun orderDate_winsOverBetweenFields() {
        val request = OrderFlexibleSearchRequest().apply {
            orderDate = "2026-08-06"
            fromDate = "2026-01-01"
            toDate = "2026-12-31"
        }

        val criteria = OrderFlexibleSearchCriteriaBuilder.from(request)
        assertThat(criteria.createdFrom).isEqualTo(LocalDateTime.of(2026, 8, 6, 0, 0))
        assertThat(criteria.createdTo).isEqualTo(LocalDateTime.of(2026, 8, 7, 0, 0))
        assertThat(criteria.createdToInclusive).isFalse()
    }

    @Test
    fun mapsEnumNumberAndString() {
        val request = OrderFlexibleSearchRequest().apply {
            customerName = " Kim "
            status = OrderStatus.PENDING
            payMethod = PayMethod.CARD
            minQuantity = 3
        }

        val criteria = OrderFlexibleSearchCriteriaBuilder.from(request)
        assertThat(criteria.customerName).isEqualTo("Kim")
        assertThat(criteria.status).isEqualTo(OrderStatus.PENDING)
        assertThat(criteria.payMethod).isEqualTo(PayMethod.CARD)
        assertThat(criteria.minQuantity).isEqualTo(3)
    }
}
