package com.jpsoftware.farmapp.integration.production;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.fixture.AnimalFixture;
import com.jpsoftware.farmapp.fixture.ProductionFixture;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class ProductionIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateFetchUpdateAndSummarizeProductionThroughRealSpringContext() throws Exception {
        UserEntity savedUser = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "ADMIN"));
        animalRepository.save(AnimalFixture.animalEntity());
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 2.0, true));
        feedingRepository.save(new FeedingEntity(
                null,
                "animal-1",
                feedType.getId(),
                java.time.LocalDate.of(2026, 3, 20),
                10.0,
                savedUser.getId().toString()));

        MvcResult createdResult = mockMvc.perform(post("/productions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ProductionFixture.createRequestJson("animal-1", savedUser.getId().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.quantity").value(12.5))
                .andReturn();

        String productionId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/productions/{id}", productionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productionId))
                .andExpect(jsonPath("$.animalId").value("animal-1"));

        mockMvc.perform(put("/productions/{id}", productionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ProductionFixture.updateRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-03-21"))
                .andExpect(jsonPath("$.quantity").value(15.0));

        mockMvc.perform(get("/productions/summary/by-animal").param("animalId", "animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.totalQuantity").value(15.0));

        mockMvc.perform(get("/productions/summary/profit/by-animal").param("animalId", "animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.totalProduction").value(15.0))
                .andExpect(jsonPath("$.totalFeedingCost").value(20.0))
                .andExpect(jsonPath("$.profit").value(10.0));
    }
}
