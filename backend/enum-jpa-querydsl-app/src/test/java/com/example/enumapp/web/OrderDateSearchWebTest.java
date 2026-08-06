package com.example.enumapp.web;

import com.example.enumapp.domain.order.OrderSearchCriteria;
import com.example.enumapp.domain.order.OrderService;
import com.example.enumapp.web.dto.OrderDateSearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderDateSearchWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("숫자 타입 Integer 필드에 null 이 들어오면 0 이 아니라 null 로 유지")
    void minQuantity_null_doesNotBecomeZero() throws Exception {
        String body = """
                {
                  "orderDate": "2026-08-06",
                  "minQuantity": null
                }
                """;

        OrderDateSearchRequest request = objectMapper.readValue(body, OrderDateSearchRequest.class);
        assertThat(request.getMinQuantity()).isNull();

        OrderSearchCriteria criteria = orderService.toCriteria(request);
        assertThat(criteria.getMinQuantity())
                .as("null Integer must stay null, not default to 0")
                .isNull();
        assertThat(criteria.getMinQuantity() == null || criteria.getMinQuantity() != 0).isTrue();
        assertThat(java.util.Objects.equals(criteria.getMinQuantity(), 0)).isFalse();
    }

    @Test
    @DisplayName("단일 날짜 문자열 → LocalDate 조회")
    void searchBySingleDate() throws Exception {
        seedOrder("single-day", LocalDateTime.of(2026, 8, 6, 9, 0, 0), 1);
        seedOrder("other-day", LocalDateTime.of(2026, 8, 7, 9, 0, 0), 1);

        String body = """
                {
                  "orderDate": "2026-08-06",
                  "minQuantity": null
                }
                """;

        mockMvc.perform(post("/api/orders/search/by-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].customerName").value("single-day"));
    }

    @Test
    @DisplayName("두 날짜 문자열 between → LocalDate 조회")
    void searchByDateBetween() throws Exception {
        seedOrder("d1", LocalDateTime.of(2026, 8, 1, 12, 0, 0), 1);
        seedOrder("d2", LocalDateTime.of(2026, 8, 3, 12, 0, 0), 1);
        seedOrder("d3", LocalDateTime.of(2026, 8, 10, 12, 0, 0), 1);

        String body = """
                {
                  "fromDate": "2026-08-01",
                  "toDate": "2026-08-03"
                }
                """;

        mockMvc.perform(post("/api/orders/search/by-date-between")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    @DisplayName("두 일시 문자열 between → LocalDateTime 조회")
    void searchByDateTimeBetween() throws Exception {
        seedOrder("t1", LocalDateTime.of(2026, 8, 6, 10, 0, 0), 1);
        seedOrder("t2", LocalDateTime.of(2026, 8, 6, 15, 30, 0), 1);
        seedOrder("t3", LocalDateTime.of(2026, 8, 6, 20, 0, 0), 1);

        String body = """
                {
                  "fromDateTime": "2026-08-06T09:00:00",
                  "toDateTime": "2026-08-06T16:00:00"
                }
                """;

        mockMvc.perform(post("/api/orders/search/by-datetime-between")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    @DisplayName("minQuantity 가 null 이면 수량 필터가 적용되지 않고, 값이 있으면 필터 적용")
    void minQuantity_nullSkipsFilter_valueAppliesFilter() throws Exception {
        seedOrder("q-small", LocalDateTime.of(2026, 8, 6, 11, 0, 0), 1);
        seedOrder("q-large", LocalDateTime.of(2026, 8, 6, 11, 30, 0), 10);

        MvcResult all = mockMvc.perform(post("/api/orders/search/by-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderDate":"2026-08-06","minQuantity":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andReturn();
        assertThat(objectMapper.readTree(all.getResponse().getContentAsString()).get("result")).hasSize(2);

        mockMvc.perform(post("/api/orders/search/by-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderDate":"2026-08-06","minQuantity":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].customerName").value("q-large"));
    }

    private void seedOrder(String customerName, LocalDateTime createdAt, int quantity) {
        var request = new com.example.enumapp.web.dto.OrderRequest();
        request.setCustomerName(customerName);
        request.setStatus(com.example.enumapp.domain.order.OrderStatus.PENDING);
        request.setPayMethod(com.example.enumapp.domain.order.PayMethod.CARD);
        request.setUserGrade(com.example.enumapp.domain.order.UserGrade.BASIC);
        var item = new com.example.enumapp.web.dto.OrderRequest.OrderItemRequest();
        item.setProductName("Item");
        item.setSku("SKU");
        item.setQuantity(quantity);
        request.setItems(java.util.List.of(item));
        orderService.createWithCreatedAt(request, createdAt);
    }
}
