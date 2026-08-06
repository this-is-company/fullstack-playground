package com.example.enumapp.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileUploadDemoWebTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("temp 업로드 → commit → USED")
    fun uploadTemp_thenCommit() {
        val file = MockMultipartFile(
            "file", "hello.txt", "text/plain", "hello".toByteArray(),
        )

        val created = mockMvc.perform(multipart("/api/demo/files/temp").file(file))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.result.status").value("TEMP"))
            .andReturn()

        val id = objectMapper.readTree(created.response.contentAsString)
            .path("result").path("id").asLong()

        mockMvc.perform(post("/api/demo/files/{id}/commit", id).param("ownerRef", "order:1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.status").value("USED"))
            .andExpect(jsonPath("$.result.ownerRef").value("order:1"))
    }

    @Test
    @DisplayName("temp discard 후 조회 불가")
    fun discardTemp() {
        val file = MockMultipartFile(
            "file", "bye.txt", "text/plain", "bye".toByteArray(),
        )
        val id = objectMapper.readTree(
            mockMvc.perform(multipart("/api/demo/files/temp").file(file))
                .andExpect(status().isCreated)
                .andReturn()
                .response
                .contentAsString,
        ).path("result").path("id").asLong()

        mockMvc.perform(delete("/api/demo/files/{id}", id))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/demo/files/{id}", id))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("replace: 새 TEMP commit 후 옛 파일 삭제")
    fun replaceFile() {
        val oldId = upload("old.txt", "old")
        mockMvc.perform(post("/api/demo/files/{id}/commit", oldId).param("ownerRef", "order:9"))
            .andExpect(status().isOk)

        val newId = upload("new.txt", "new")

        mockMvc.perform(
            post("/api/demo/files/replace")
                .param("oldId", oldId.toString())
                .param("newId", newId.toString())
                .param("ownerRef", "order:9"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result.id").value(newId))
            .andExpect(jsonPath("$.result.status").value("USED"))

        mockMvc.perform(get("/api/demo/files/{id}", oldId))
            .andExpect(status().isNotFound)
    }

    private fun upload(name: String, content: String): Long {
        val result = mockMvc.perform(
            multipart("/api/demo/files/temp")
                .file(MockMultipartFile("file", name, "text/plain", content.toByteArray())),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val node = objectMapper.readTree(result.response.contentAsString)
        assertThat(node.path("result").path("status").asText()).isEqualTo("TEMP")
        return node.path("result").path("id").asLong()
    }
}
