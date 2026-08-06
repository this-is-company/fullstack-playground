package com.example.enumapp.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileUploadDemoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("temp 업로드 → commit → USED")
    void uploadTemp_thenCommit() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello".getBytes()
        );

        MvcResult created = mockMvc.perform(multipart("/api/demo/files/temp").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.status").value("TEMP"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("result").path("id").asLong();

        mockMvc.perform(post("/api/demo/files/{id}/commit", id).param("ownerRef", "order:1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("USED"))
                .andExpect(jsonPath("$.result.ownerRef").value("order:1"));
    }

    @Test
    @DisplayName("temp discard 후 조회 불가")
    void discardTemp() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bye.txt", "text/plain", "bye".getBytes()
        );
        long id = objectMapper.readTree(
                mockMvc.perform(multipart("/api/demo/files/temp").file(file))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).path("result").path("id").asLong();

        mockMvc.perform(delete("/api/demo/files/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/demo/files/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("replace: 새 TEMP commit 후 옛 파일 삭제")
    void replaceFile() throws Exception {
        long oldId = upload("old.txt", "old");
        mockMvc.perform(post("/api/demo/files/{id}/commit", oldId).param("ownerRef", "order:9"))
                .andExpect(status().isOk());

        long newId = upload("new.txt", "new");

        mockMvc.perform(post("/api/demo/files/replace")
                        .param("oldId", String.valueOf(oldId))
                        .param("newId", String.valueOf(newId))
                        .param("ownerRef", "order:9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(newId))
                .andExpect(jsonPath("$.result.status").value("USED"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/demo/files/{id}", oldId))
                .andExpect(status().isNotFound());
    }

    private long upload(String name, String content) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/demo/files/temp")
                        .file(new MockMultipartFile("file", name, "text/plain", content.getBytes())))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(node.path("result").path("status").asText()).isEqualTo("TEMP");
        return node.path("result").path("id").asLong();
    }
}
