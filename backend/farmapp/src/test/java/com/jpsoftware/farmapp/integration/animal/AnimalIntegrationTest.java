package com.jpsoftware.farmapp.integration.animal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.fixture.AnimalFixture;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AnimalIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateFetchUpdateAndDeleteAnimalThroughRealSpringContext() throws Exception {
        MvcResult createdResult = mockMvc.perform(post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.createRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tag").value("TAG-001"))
                .andReturn();

        String animalId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/animals/{id}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(animalId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(put("/animals/{id}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.updateRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("TAG-002"))
                .andExpect(jsonPath("$.farmId").value("FARM-002"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/animals/{id}", animalId))
                .andExpect(status().isNoContent());

        assertFalse(animalRepository.existsById(animalId));
    }
}
