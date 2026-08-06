package com.example.enumapp.web

import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderResourceControllerWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("PUT 생성 → GET 조회 → PATCH 수정 → POST cancel → DELETE")
    fun singleResourceFlow() {
        val createBody = """
            {
              "customerName": "Resource-Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()

        val created = mockMvc.perform(put(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.customerName").value("Resource-Kim"))
            .andReturn()

        val id: Int = JsonPath.read(created.response.contentAsString, "$.result.id")

        mockMvc.perform(get("$BASE/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.id").value(id))

        val patchBody = """
            {
              "customerName": "Resource-Kim-updated",
              "userGrade": "G",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 2}
              ]
            }
        """.trimIndent()
        mockMvc.perform(patch("$BASE/$id").contentType(MediaType.APPLICATION_JSON).content(patchBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.customerName").value("Resource-Kim-updated"))
            .andExpect(jsonPath("$.result.status").value("P"))

        mockMvc.perform(post("$BASE/$id/cancel"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.status").value("C"))

        mockMvc.perform(delete("$BASE/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))

        mockMvc.perform(get("$BASE/$id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.ORDER_NOT_FOUND))
    }

    @Test
    @DisplayName("POST 기본 목록 / 다건 수정 / 다건 삭제")
    fun collectionFlow() {
        val createBody = """
            {
              "customerName": "Bulk-%s",
              "status": "P",
              "payMethod": "CASH",
              "userGrade": "S",
              "items": [
                {"productName": "Cup", "sku": "C-01", "quantity": 1}
              ]
            }
        """.trimIndent()

        val id1 = create(createBody.format("A"))
        val id2 = create(createBody.format("B"))

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.length()").value(greaterThanOrEqualTo(2)))

        val bulkUpdate = """{"ids": [$id1, $id2], "status": "A"}"""
        mockMvc.perform(post("$BASE/update").contentType(MediaType.APPLICATION_JSON).content(bulkUpdate))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.length()").value(2))
            .andExpect(jsonPath("$.result[0].status").value("A"))

        val bulkDelete = """{"ids": [$id1, $id2]}"""
        mockMvc.perform(post("$BASE/delete").contentType(MediaType.APPLICATION_JSON).content(bulkDelete))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.error").value(false))

        mockMvc.perform(get("$BASE/$id1"))
            .andExpect(status().isNotFound)
    }

    private fun create(body: String): Int {
        val created = mockMvc.perform(put(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated)
            .andReturn()
        return JsonPath.read(created.response.contentAsString, "$.result.id")
    }

    companion object {
        private const val BASE = "/api/resource/orders"
    }
}
