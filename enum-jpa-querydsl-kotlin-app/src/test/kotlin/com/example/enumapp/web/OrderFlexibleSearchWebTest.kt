package com.example.enumapp.web

import com.example.enumapp.domain.order.OrderService
import com.example.enumapp.domain.order.OrderStatus
import com.example.enumapp.domain.order.PayMethod
import com.example.enumapp.domain.order.UserGrade
import com.example.enumapp.web.dto.OrderRequest
import org.hamcrest.Matchers.greaterThanOrEqualTo
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderFlexibleSearchWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var orderService: OrderService

    @Test
    @DisplayName("모든 조건이 비어 있으면 전체 목록")
    fun emptyCriteria_returnsAll() {
        seed("Flex-A", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 8, 1, 10, 0))
        seed("Flex-B", OrderStatus.PAID, PayMethod.CASH, 5, LocalDateTime.of(2026, 8, 5, 10, 0))

        mockMvc.perform(
            post("/api/orders/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.length()").value(greaterThanOrEqualTo(2)))

        mockMvc.perform(
            post("/api/orders/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "customerName": null,
                      "status": null,
                      "payMethod": null,
                      "minQuantity": null,
                      "orderDate": "",
                      "fromDate": null,
                      "toDate": "",
                      "fromDateTime": null,
                      "toDateTime": null
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.length()").value(greaterThanOrEqualTo(2)))
    }

    @Test
    @DisplayName("문자열/enum/숫자/날짜 조건을 조합하면 AND 필터")
    fun combinedOptionalFilters() {
        seed("Alpha-Kim", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 8, 2, 9, 0))
        seed("Alpha-Kim", OrderStatus.PENDING, PayMethod.CARD, 10, LocalDateTime.of(2026, 8, 2, 15, 0))
        seed("Beta-Lee", OrderStatus.PAID, PayMethod.CASH, 10, LocalDateTime.of(2026, 8, 2, 12, 0))

        mockMvc.perform(
            post("/api/orders/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "customerName": "Kim",
                      "status": "P",
                      "payMethod": "CARD",
                      "minQuantity": 5,
                      "orderDate": "2026-08-02"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.length()").value(1))
            .andExpect(jsonPath("$.result[0].customerName").value("Alpha-Kim"))
            .andExpect(jsonPath("$.result[0].items[0].quantity").value(10))
    }

    @Test
    @DisplayName("fromDate/toDate between 만으로도 조회")
    fun dateBetweenOnly() {
        seed("D1", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 7, 1, 10, 0))
        seed("D2", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 7, 5, 10, 0))
        seed("D3", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 7, 20, 10, 0))

        mockMvc.perform(
            post("/api/orders/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "fromDate": "2026-07-01",
                      "toDate": "2026-07-05"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.length()").value(2))
    }

    private fun seed(
        name: String,
        status: OrderStatus,
        pay: PayMethod,
        qty: Int,
        createdAt: LocalDateTime
    ) {
        val request = OrderRequest().apply {
            customerName = name
            this.status = status
            payMethod = pay
            userGrade = UserGrade.BASIC
            items = listOf(
                OrderRequest.OrderItemRequest().apply {
                    productName = "Item"
                    sku = "SKU-1"
                    quantity = qty
                }
            )
        }
        orderService.createWithCreatedAt(request, createdAt)
    }
}
