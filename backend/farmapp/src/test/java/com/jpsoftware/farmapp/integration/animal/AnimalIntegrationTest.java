package com.jpsoftware.farmapp.integration.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.fixture.AnimalFixture;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AnimalIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateFetchUpdateAndDeleteAnimalThroughRealSpringContext() throws Exception {
        UserEntity user = createAuthenticatedUser();
        FarmEntity sourceFarm = createFarmOwnedBy(user, "North Dairy");
        FarmEntity destinationFarm = createFarmOwnedBy(user, "South Dairy");
        String authorization = bearerToken(user);

        MvcResult createdResult = mockMvc.perform(post("/animals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.createRequestJson(sourceFarm.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tag").value("TAG-001"))
                .andReturn();

        String animalId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/animals/{id}", animalId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(animalId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(put("/animals/{id}", animalId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.updateRequestJson(destinationFarm.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("TAG-002"))
                .andExpect(jsonPath("$.farmId").value(destinationFarm.getId()))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/animals/{id}", animalId)
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        AnimalEntity deletedAnimal = animalRepository.findById(animalId).orElseThrow();
        assertEquals(AnimalEntity.STATUS_INACTIVE, deletedAnimal.getStatus());
    }

    @Test
    void shouldFailWhenTagAlreadyExists() throws Exception {
        UserEntity user = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(user, "North Dairy");
        String authorization = bearerToken(user);

        mockMvc.perform(post("/animals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.createRequestJson(farm.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/animals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.createRequestJson(farm.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Animal with this tag already exists"));
    }
}
