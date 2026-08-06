package com.example.enumapp.web

import org.hamcrest.Matchers.containsString
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RequestPatternValidationWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("LocalDate 패턴 불일치 / 존재하지 않는 날짜는 400")
    fun localDate_patternAndCalendarValidated() {
        mockMvc.perform(
            post("/api/orders/search/by-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderDate":"06/08/2026"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.VALIDATION_ERROR))
            .andExpect(jsonPath("$.result.errors.orderDate", containsString("yyyy-MM-dd")))

        mockMvc.perform(
            post("/api/orders/search/by-date")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderDate":"2026-02-30"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.orderDate", containsString("yyyy-MM-dd")))
    }

    @Test
    @DisplayName("LocalDateTime 패턴 불일치(공백 구분, 초 없음)는 400")
    fun localDateTime_patternValidated() {
        mockMvc.perform(
            post("/api/orders/search/by-datetime-between")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "fromDateTime":"2026-08-06 09:00:00",
                      "toDateTime":"2026-08-06T18:00:00"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.fromDateTime", containsString("yyyy-MM-dd'T'HH:mm:ss")))

        mockMvc.perform(
            post("/api/orders/search/by-datetime-between")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "fromDateTime":"2026-08-06T09:00",
                      "toDateTime":"2026-08-06T18:00:00"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.fromDateTime", containsString("yyyy-MM-dd'T'HH:mm:ss")))
    }

    @Test
    @DisplayName("String 필드에 특수문자가 있으면 400")
    fun string_rejectsSpecialCharacters() {
        val body = """
            {
              "customerName": "Kim<script>",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.VALIDATION_ERROR))
            .andExpect(jsonPath("$.result.errors.customerName", containsString("special characters")))

        val skuBody = """
            {
              "customerName": "Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU@1!", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(skuBody))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors['items[0].sku']", containsString("special characters")))
    }

    @Test
    @DisplayName("@Pattern 필드(promoCode) 형식 불일치면 400")
    fun promoCode_patternValidated() {
        val body = """
            {
              "customerName": "Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "promoCode": "PROMO@BAD!",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.promoCode", containsString("promoCode must match pattern")))
    }
}
