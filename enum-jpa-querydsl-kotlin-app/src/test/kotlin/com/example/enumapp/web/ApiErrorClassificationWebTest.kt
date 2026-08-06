package com.example.enumapp.web

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiErrorClassificationWebTest.BoomController::class)
class ApiErrorClassificationWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("비즈니스 오류는 4xx + messageCode")
    fun businessError_is4xx() {
        mockMvc.perform(get("/api/orders/999999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.ORDER_NOT_FOUND))
            .andExpect(jsonPath("$.result.message", containsString("order not found")))
    }

    @Test
    @DisplayName("서버 오류는 5xx + INTERNAL_ERROR (상세는 노출하지 않음)")
    fun serverError_is5xx() {
        mockMvc.perform(get("/__test__/boom"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.INTERNAL_ERROR))
            .andExpect(jsonPath("$.result.message").value("Internal server error"))
    }

    @Test
    @DisplayName("BusinessException 은 4xx 만 허용")
    fun businessException_rejects5xxStatus() {
        assertThrows(IllegalArgumentException::class.java) {
            BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "X", "y")
        }
    }

    @RestController
    class BoomController {
        @GetMapping("/__test__/boom")
        fun boom() {
            throw IllegalStateException("secret db password leaked")
        }
    }
}
