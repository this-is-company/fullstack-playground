package com.example.enumapp.web;

import com.example.enumapp.domain.order.OrderService;
import com.example.enumapp.domain.order.OrderStatus;
import com.example.enumapp.domain.order.PayMethod;
import com.example.enumapp.domain.order.UserGrade;
import com.example.enumapp.web.dto.OrderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderFlexibleSearchWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("모든 조건이 비어 있으면 전체 목록")
    void emptyCriteria_returnsAll() throws Exception {
        seed("Flex-A", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 8, 1, 10, 0));
        seed("Flex-B", OrderStatus.PAID, PayMethod.CASH, 5, LocalDateTime.of(2026, 8, 5, 10, 0));

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.length()").value(greaterThanOrEqualTo(2)));

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
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
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("문자열/enum/숫자/날짜 조건을 조합하면 AND 필터")
    void combinedOptionalFilters() throws Exception {
        seed("Alpha-Kim", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 8, 2, 9, 0));
        seed("Alpha-Kim", OrderStatus.PENDING, PayMethod.CARD, 10, LocalDateTime.of(2026, 8, 2, 15, 0));
        seed("Beta-Lee", OrderStatus.PAID, PayMethod.CASH, 10, LocalDateTime.of(2026, 8, 2, 12, 0));

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Kim",
                                  "status": "P",
                                  "payMethod": "CARD",
                                  "minQuantity": 5,
                                  "orderDate": "2026-08-02"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].customerName").value("Alpha-Kim"))
                .andExpect(jsonPath("$.result[0].items[0].quantity").value(10));
    }

    @Test
    @DisplayName("fromDate/toDate between 만으로도 조회")
    void dateBetweenOnly() throws Exception {
        seed("D1", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 7, 1, 10, 0));
        seed("D2", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 7, 5, 10, 0));
        seed("D3", OrderStatus.PENDING, PayMethod.CARD, 1, LocalDateTime.of(2026, 7, 20, 10, 0));

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromDate": "2026-07-01",
                                  "toDate": "2026-07-05"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    private void seed(String name, OrderStatus status, PayMethod pay, int qty, LocalDateTime createdAt) {
        OrderRequest request = new OrderRequest();
        request.setCustomerName(name);
        request.setStatus(status);
        request.setPayMethod(pay);
        request.setUserGrade(UserGrade.BASIC);
        var item = new OrderRequest.OrderItemRequest();
        item.setProductName("Item");
        item.setSku("SKU-1");
        item.setQuantity(qty);
        request.setItems(List.of(item));
        orderService.createWithCreatedAt(request, createdAt);
    }
}
