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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "spring.cache.type=caffeine"
})
class OrderCacheWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("GET 단건 두 번 호출 + 캐시 stats hit 증가")
    void getById_usesCache() throws Exception {
        Long id = seedOrder("Cache-User");

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.customerName").value("Cache-User"));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/demo/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.stats.orders.hitCount")
                        .value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("유연 검색 빈 조건 — orders-list 캐시")
    void searchEmpty_usesListCache() throws Exception {
        seedOrder("List-A");

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/demo/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.stats.orders-list.hitCount").value(1));
    }

    @Test
    @DisplayName("@CachePut — 이름 변경 후 GET 은 캐시의 새 값")
    void cachePut_overwritesEntry() throws Exception {
        Long id = seedOrder("Before-Put");

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(jsonPath("$.result.customerName").value("Before-Put"));

        mockMvc.perform(put("/api/demo/cache/orders/{id}", id).param("customerName", "After-Put"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.customerName").value("After-Put"));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.customerName").value("After-Put"));
    }

    @Test
    @DisplayName("@CacheEvict — 캐시만 지우고 DB 행은 남김")
    void cacheEvict_clearsKeyOnly() throws Exception {
        Long id = seedOrder("Evict-User");

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/demo/cache/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.evicted").value(true));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.customerName").value("Evict-User"));
    }

    private Long seedOrder(String customerName) {
        OrderRequest request = new OrderRequest();
        request.setCustomerName(customerName);
        request.setStatus(OrderStatus.PENDING);
        request.setPayMethod(PayMethod.CARD);
        request.setUserGrade(UserGrade.BASIC);
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductName("P");
        item.setSku("SKU-C");
        item.setQuantity(1);
        request.setItems(List.of(item));
        return orderService.create(request).getId();
    }
}
