package com.example.enumapp.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetValidationDemoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("정상 GET: 리스트/중첩/숫자/날짜 검증 후 LocalDate 변환")
    void search_ok_convertsDates() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/search")
                        .param("ids", "1", "2")
                        .param("items[0].sku", "ABC-1")
                        .param("items[0].quantity", "3")
                        .param("minQuantity", "5")
                        .param("orderDate", "2026-08-06")
                        .param("fromDateTime", "2026-08-06T09:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.ids[0]").value(1))
                .andExpect(jsonPath("$.result.items[0].sku").value("ABC-1"))
                .andExpect(jsonPath("$.result.minQuantity").value(5))
                .andExpect(jsonPath("$.result.orderDate").value("2026-08-06"))
                .andExpect(jsonPath("$.result.fromDateTime").value("2026-08-06T09:00:00"));
    }

    @Test
    @DisplayName("ids 누락 → 400")
    void search_missingIds() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/search")
                        .param("items[0].sku", "ABC")
                        .param("items[0].quantity", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true))
                .andExpect(jsonPath("$.result.messageCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.result.errors", hasKey("ids")));
    }

    @Test
    @DisplayName("items 내부 quantity null/누락 → 400")
    void search_itemQuantityMissing() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/search")
                        .param("ids", "1")
                        .param("items[0].sku", "ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.errors", hasKey("items[0].quantity")));
    }

    @Test
    @DisplayName("잘못된 날짜 문자열 → 400")
    void search_badDate() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/search")
                        .param("ids", "1")
                        .param("items[0].sku", "ABC")
                        .param("items[0].quantity", "1")
                        .param("orderDate", "2026-13-40"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.messageCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("음수 minQuantity → 400")
    void search_negativeMinQuantity() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/search")
                        .param("ids", "1")
                        .param("items[0].sku", "ABC")
                        .param("items[0].quantity", "1")
                        .param("minQuantity", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.errors", hasKey("minQuantity")));
    }

    @Test
    @DisplayName("@DateTimeFormat 으로 LocalDate 직접 바인딩")
    void typedDates_ok() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/typed-dates")
                        .param("orderDate", "2026-08-06")
                        .param("fromDateTime", "2026-08-06T09:00:00")
                        .param("minQuantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.orderDate").value("2026-08-06"))
                .andExpect(jsonPath("$.result.orderDateType").value("LocalDate"))
                .andExpect(jsonPath("$.result.fromDateTimeType").value("LocalDateTime"))
                .andExpect(jsonPath("$.result.minQuantity").value(3));
    }

    @Test
    @DisplayName("typed-dates 잘못된 형식 → 400 type mismatch")
    void typedDates_badFormat() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/typed-dates")
                        .param("orderDate", "08/06/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true));
    }

    @Test
    @DisplayName("required-ids 비어 있으면 400")
    void requiredIds_empty() throws Exception {
        mockMvc.perform(get("/api/demo/get-validation/required-ids"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result.messageCode").value("VALIDATION_ERROR"));
    }
}
