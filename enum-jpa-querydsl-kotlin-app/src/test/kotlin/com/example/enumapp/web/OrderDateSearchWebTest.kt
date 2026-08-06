package com.example.enumapp.web

import com.example.enumapp.domain.order.OrderService
import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.domain.order.UserGrade
import com.example.enumapp.web.dto.OrderDateSearchRequest
import com.example.enumapp.web.dto.OrderRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Objects

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderDateSearchWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var orderService: OrderService

    @Test
    @DisplayName("숫자 타입 Integer 필드에 null 이 들어오면 0 이 아니라 null 로 유지")
    fun minQuantity_null_doesNotBecomeZero() {
        val body = """
            {
              "orderDate": "2026-08-06",
              "minQuantity": null
            }
        """.trimIndent()

        val request = objectMapper.readValue(body, OrderDateSearchRequest::class.java)
        assertThat(request.minQuantity).isNull()

        val criteria = orderService.toCriteria(request)
        assertThat(criteria.minQuantity)
            .`as`("null Integer must stay null, not default to 0")
            .isNull()
        assertThat(criteria.minQuantity == null || criteria.minQuantity != 0).isTrue()
        assertThat(Objects.equals(criteria.minQuantity, 0)).isFalse()
    }

    @Test
    @DisplayName("단일 날짜 문자열 → LocalDate 조회")
    fun searchBySingleDate() {
        seedOrder("single-day", LocalDateTime.of(2026, 8, 6, 9, 0, 0), 1)
        seedOrder("other-day", LocalDateTime.of(2026, 8, 7, 9, 0, 0), 1)

        val body = """
            {
              "orderDate": "2026-08-06",
              "minQuantity": null
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/orders/search/by-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].customerName").value("single-day"))
    }

    @Test
    @DisplayName("두 날짜 문자열 between → LocalDate 조회")
    fun searchByDateBetween() {
        seedOrder("d1", LocalDateTime.of(2026, 8, 1, 12, 0, 0), 1)
        seedOrder("d2", LocalDateTime.of(2026, 8, 3, 12, 0, 0), 1)
        seedOrder("d3", LocalDateTime.of(2026, 8, 10, 12, 0, 0), 1)

        val body = """
            {
              "fromDate": "2026-08-01",
              "toDate": "2026-08-03"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/orders/search/by-date-between")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.length()").value(2))
    }

    @Test
    @DisplayName("두 일시 문자열 between → LocalDateTime 조회")
    fun searchByDateTimeBetween() {
        seedOrder("t1", LocalDateTime.of(2026, 8, 6, 10, 0, 0), 1)
        seedOrder("t2", LocalDateTime.of(2026, 8, 6, 15, 30, 0), 1)
        seedOrder("t3", LocalDateTime.of(2026, 8, 6, 20, 0, 0), 1)

        val body = """
            {
              "fromDateTime": "2026-08-06T09:00:00",
              "toDateTime": "2026-08-06T16:00:00"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/orders/search/by-datetime-between")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.length()").value(2))
    }

    @Test
    @DisplayName("minQuantity 가 null 이면 수량 필터가 적용되지 않고, 값이 있으면 필터 적용")
    fun minQuantity_nullSkipsFilter_valueAppliesFilter() {
        seedOrder("q-small", LocalDateTime.of(2026, 8, 6, 11, 0, 0), 1)
        seedOrder("q-large", LocalDateTime.of(2026, 8, 6, 11, 30, 0), 10)

        val all = mockMvc.perform(
            post("/api/orders/search/by-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderDate":"2026-08-06","minQuantity":null}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))
            .andReturn()
        assertThat(objectMapper.readTree(all.response.contentAsString).get("result")).hasSize(2)

        mockMvc.perform(
            post("/api/orders/search/by-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderDate":"2026-08-06","minQuantity":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].customerName").value("q-large"))
    }

    private fun seedOrder(customerName: String, createdAt: LocalDateTime, quantity: Int) {
        val request = OrderRequest().apply {
            this.customerName = customerName
            status = OrderStatus.PENDING
            payMethod = PayMethod.CARD
            userGrade = UserGrade.BASIC
            items = listOf(
                OrderRequest.OrderItemRequest().apply {
                    productName = "Item"
                    sku = "SKU"
                    this.quantity = quantity
                }
            )
        }
        orderService.createWithCreatedAt(request, createdAt)
    }
}
