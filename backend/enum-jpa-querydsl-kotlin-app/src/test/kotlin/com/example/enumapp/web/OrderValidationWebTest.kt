package com.example.enumapp.web

import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderValidationWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("Create: items list 자체가 null 이면 400")
    fun create_fails_whenItemsListIsNull() {
        val body = """
            {
              "customerName": "Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": null
            }
        """.trimIndent()

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.VALIDATION_ERROR))
            .andExpect(jsonPath("$.result.message").value("Validation failed"))
            .andExpect(jsonPath("$.result.errors.items", containsString("must not be null")))
    }

    @Test
    @DisplayName("Create: list 내부 productName 이 null/blank 이면 400")
    fun create_fails_whenItemFieldBlankOrNull() {
        val blankName = """
            {
              "customerName": "Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(blankName))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.VALIDATION_ERROR))

        val nullSku = """
            {
              "customerName": "Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": null, "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(nullSku))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(true))
    }

    @Test
    @DisplayName("Create: enum null/blank 는 역직렬화 후 @NotNull 로 400")
    fun create_fails_whenRequiredEnumNullOrBlank() {
        val nullStatus = """
            {
              "customerName": "Kim",
              "status": null,
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(nullStatus))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.errors.status", containsString("required on create")))

        val blankPayMethod = """
            {
              "customerName": "Kim",
              "status": "P",
              "payMethod": "",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(blankPayMethod))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.payMethod", containsString("required on create")))
    }

    @Test
    @DisplayName("Create group: id 가 있으면 400 / Update group: status 없어도 되고 Create 는 status 필수")
    fun validationGroups_createAndUpdate_differ() {
        val createWithId = """
            {
              "id": 1,
              "customerName": "Kim",
              "status": "P",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(createWithId))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.id", containsString("must be null on create")))

        val createWithoutStatus = """
            {
              "customerName": "Kim",
              "payMethod": "CARD",
              "userGrade": "B",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(createWithoutStatus))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.result.errors.status", containsString("required on create")))

        val updateWithoutStatusButWithName = """
            {
              "customerName": "Kim-updated",
              "userGrade": "S",
              "items": [
                {"productName": "Book", "sku": "SKU-1", "quantity": 1}
              ]
            }
        """.trimIndent()
        val result = mockMvc.perform(
            put("/api/orders/99999").contentType(MediaType.APPLICATION_JSON).content(updateWithoutStatusButWithName)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.ORDER_NOT_FOUND))
            .andReturn()
        assertThat(result.response.contentAsString).contains("order not found")
    }

    @Test
    @DisplayName("Create 성공 시 enum code 저장/조회 및 description 변환")
    fun create_success_convertsEnumsInResponse() {
        val body = """
            {
              "customerName": "Lee",
              "status": "P",
              "payMethod": "TRANSFER",
              "userGrade": "G",
              "items": [
                {"productName": "Laptop", "sku": "L-01", "quantity": 2}
              ]
            }
        """.trimIndent()

        val created = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.status").value("P"))
            .andExpect(jsonPath("$.result.statusDescription").value("대기"))
            .andExpect(jsonPath("$.result.payMethod").value("TRANSFER"))
            .andExpect(jsonPath("$.result.payMethodDescription").value("계좌이체"))
            .andExpect(jsonPath("$.result.userGrade").value("G"))
            .andExpect(jsonPath("$.result.userGradeDescription").value("골드"))
            .andExpect(jsonPath("$.result.items[0].productName").value("Laptop"))
            .andReturn()

        val id = JsonPath.read<Any>(created.response.contentAsString, "$.result.id").toString()

        mockMvc.perform(get("/api/orders/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.status").value("P"))
            .andExpect(jsonPath("$.result.payMethod").value("TRANSFER"))
    }

    @Test
    @DisplayName("Update: status/payMethod null(또는 blank) 허용, Create 와 규칙이 다름")
    fun update_allowsNullOptionalEnums_unlikeCreate() {
        val createBody = """
            {
              "customerName": "Park",
              "status": "P",
              "payMethod": "CASH",
              "userGrade": "S",
              "items": [
                {"productName": "Cup", "sku": "C-01", "quantity": 3}
              ]
            }
        """.trimIndent()
        val created = mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(createBody))
            .andExpect(status().isCreated)
            .andReturn()
        val id: Int = JsonPath.read(created.response.contentAsString, "$.result.id")

        val updateBody = """
            {
              "customerName": "Park-updated",
              "status": "",
              "payMethod": null,
              "userGrade": "G",
              "items": [
                {"productName": "Cup", "sku": "C-01", "quantity": 5}
              ]
            }
        """.trimIndent()
        mockMvc.perform(put("/api/orders/$id").contentType(MediaType.APPLICATION_JSON).content(updateBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.error").value(false))
            .andExpect(jsonPath("$.result.customerName").value("Park-updated"))
            .andExpect(jsonPath("$.result.status").value("P"))
            .andExpect(jsonPath("$.result.payMethod").value("CASH"))
            .andExpect(jsonPath("$.result.userGrade").value("G"))
    }

    @Test
    @DisplayName("Update: items list null 이면 Create 와 동일하게 400")
    fun update_fails_whenItemsNull() {
        val body = """
            {
              "customerName": "X",
              "userGrade": "B",
              "items": null
            }
        """.trimIndent()
        mockMvc.perform(put("/api/orders/1").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value(true))
            .andExpect(jsonPath("$.result.messageCode").value(ApiMessageCodes.VALIDATION_ERROR))
    }
}
