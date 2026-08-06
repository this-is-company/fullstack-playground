package com.example.enumapp.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiErrorClassificationWebTest.BoomController.class)
class ApiErrorClassificationWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("비즈니스 오류는 4xx + messageCode")
    void businessError_is4xx() throws Exception {
        mockMvc.perform(get("/api/orders/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.ORDER_NOT_FOUND))
                .andExpect(jsonPath("$.result.message", containsString("order not found")));
    }

    @Test
    @DisplayName("서버 오류는 5xx + INTERNAL_ERROR (상세는 노출하지 않음)")
    void serverError_is5xx() throws Exception {
        mockMvc.perform(get("/__test__/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.INTERNAL_ERROR))
                .andExpect(jsonPath("$.result.message").value("Internal server error"));
    }

    @Test
    @DisplayName("BusinessException 은 4xx 만 허용")
    void businessException_rejects5xxStatus() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "X", "y")
        );
    }

    @RestController
    static class BoomController {
        @GetMapping("/__test__/boom")
        public void boom() {
            throw new IllegalStateException("secret db password leaked");
        }
    }
}
