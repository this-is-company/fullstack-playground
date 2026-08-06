package com.example.enumapp.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderResourceControllerWebTest {

    private static final String BASE = "/api/resource/orders";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("PUT 생성 → GET 조회 → PATCH 수정 → POST cancel → DELETE")
    void singleResourceFlow() throws Exception {
        String createBody = """
                {
                  "customerName": "Resource-Kim",
                  "status": "P",
                  "payMethod": "CARD",
                  "userGrade": "B",
                  "items": [
                    {"productName": "Book", "sku": "SKU-1", "quantity": 1}
                  ]
                }
                """;

        MvcResult created = mockMvc.perform(put(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.customerName").value("Resource-Kim"))
                .andReturn();

        Integer id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.result.id");

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(id));

        String patchBody = """
                {
                  "customerName": "Resource-Kim-updated",
                  "userGrade": "G",
                  "items": [
                    {"productName": "Book", "sku": "SKU-1", "quantity": 2}
                  ]
                }
                """;
        mockMvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(patchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.customerName").value("Resource-Kim-updated"))
                .andExpect(jsonPath("$.result.status").value("P"));

        mockMvc.perform(post(BASE + "/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("C"));

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false));

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("POST 기본 목록 / 다건 수정 / 다건 삭제")
    void collectionFlow() throws Exception {
        String createBody = """
                {
                  "customerName": "Bulk-%s",
                  "status": "P",
                  "payMethod": "CASH",
                  "userGrade": "S",
                  "items": [
                    {"productName": "Cup", "sku": "C-01", "quantity": 1}
                  ]
                }
                """;

        Integer id1 = create(String.format(createBody, "A"));
        Integer id2 = create(String.format(createBody, "B"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false))
                .andExpect(jsonPath("$.result.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

        String bulkUpdate = """
                {"ids": [%d, %d], "status": "A"}
                """.formatted(id1, id2);
        mockMvc.perform(post(BASE + "/update").contentType(MediaType.APPLICATION_JSON).content(bulkUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].status").value("A"));

        String bulkDelete = """
                {"ids": [%d, %d]}
                """.formatted(id1, id2);
        mockMvc.perform(post(BASE + "/delete").contentType(MediaType.APPLICATION_JSON).content(bulkDelete))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(false));

        mockMvc.perform(get(BASE + "/" + id1))
                .andExpect(status().isNotFound());
    }

    private Integer create(String body) throws Exception {
        MvcResult created = mockMvc.perform(put(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.result.id");
    }
}
