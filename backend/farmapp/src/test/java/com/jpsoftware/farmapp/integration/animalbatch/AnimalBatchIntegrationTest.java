package com.jpsoftware.farmapp.integration.animalbatch;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.fixture.AnimalFixture;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AnimalBatchIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateUpdateListAndDeleteAnimalBatch() throws Exception {
        UserEntity savedUser = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(savedUser, "North Dairy");
        String authorization = bearerToken(savedUser);

        animalRepository.save(AnimalFixture.animalEntity("animal-1", "TAG-001", "Angus", java.time.LocalDate.of(2022, 1, 10), "ACTIVE", farm.getId()));
        animalRepository.save(AnimalFixture.animalEntity("animal-2", "TAG-002", "Holstein", java.time.LocalDate.of(2022, 2, 11), "ACTIVE", farm.getId()));

        MvcResult createdResult = mockMvc.perform(post("/animal-batches")
                        .header("Authorization", authorization)
                        .param("farmId", farm.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lote A",
                                  "animalIds": ["animal-1", "animal-2"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lote A"))
                .andExpect(jsonPath("$.farmId").value(farm.getId()))
                .andExpect(jsonPath("$.animals.length()").value(2))
                .andReturn();

        String batchId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/animal-batches")
                        .header("Authorization", authorization)
                        .param("farmId", farm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(batchId))
                .andExpect(jsonPath("$[0].animals.length()").value(2));

        mockMvc.perform(put("/animal-batches/{id}", batchId)
                        .header("Authorization", authorization)
                        .param("farmId", farm.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lote B",
                                  "animalIds": ["animal-2"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lote B"))
                .andExpect(jsonPath("$.animals.length()").value(1))
                .andExpect(jsonPath("$.animals[0].id").value("animal-2"));

        mockMvc.perform(delete("/animal-batches/{id}", batchId)
                        .header("Authorization", authorization)
                        .param("farmId", farm.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/animal-batches/{id}", batchId)
                        .header("Authorization", authorization)
                        .param("farmId", farm.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectAnimalBatchWithAnimalsFromDifferentFarms() throws Exception {
        UserEntity savedUser = createAuthenticatedUser();
        FarmEntity firstFarm = createFarmOwnedBy(savedUser, "North Dairy");
        FarmEntity secondFarm = createFarmOwnedBy(savedUser, "South Dairy");
        String authorization = bearerToken(savedUser);

        animalRepository.save(AnimalFixture.animalEntity("animal-1", "TAG-001", "Angus", java.time.LocalDate.of(2022, 1, 10), "ACTIVE", firstFarm.getId()));
        animalRepository.save(AnimalFixture.animalEntity("animal-2", "TAG-002", "Holstein", java.time.LocalDate.of(2022, 2, 11), "ACTIVE", secondFarm.getId()));

        mockMvc.perform(post("/animal-batches")
                        .header("Authorization", authorization)
                        .param("farmId", firstFarm.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lote inválido",
                                  "animalIds": ["animal-1", "animal-2"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Todos os animais do lote devem pertencer à mesma fazenda."));
    }
}
