package com.jpsoftware.farmapp.integration.feeding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.fixture.AnimalFixture;
import com.jpsoftware.farmapp.fixture.FeedingFixture;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class FeedingIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnFeedingWithAnimalAndFeedTypeSummary() throws Exception {
        UserEntity savedUser = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(savedUser, "North Dairy");
        String authorization = bearerToken(savedUser);
        animalRepository.save(AnimalFixture.animalEntity("animal-1", "TAG-001", "Angus", java.time.LocalDate.of(2022, 1, 10), "ACTIVE", farm.getId()));
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 1.75, true, farm.getId()));

        MvcResult createdResult = mockMvc.perform(post("/feedings")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.createRequestJson("animal-1", feedType.getId(), savedUser.getId().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value(feedType.getId()))
                .andExpect(jsonPath("$.date").value("2026-03-24"))
                .andExpect(jsonPath("$.animal.id").value("animal-1"))
                .andExpect(jsonPath("$.animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$.feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$.feedType.name").value("Corn Silage"))
                .andReturn();

        String feedingId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/feedings/{id}", feedingId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedingId))
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value(feedType.getId()))
                .andExpect(jsonPath("$.animal.id").value("animal-1"))
                .andExpect(jsonPath("$.animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$.feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$.feedType.name").value("Corn Silage"));
    }

    @Test
    void shouldCreateFetchAndListFeedingThroughRealSpringContext() throws Exception {
        UserEntity savedUser = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(savedUser, "North Dairy");
        String authorization = bearerToken(savedUser);
        animalRepository.save(AnimalFixture.animalEntity("animal-1", "TAG-001", "Angus", java.time.LocalDate.of(2022, 1, 10), "ACTIVE", farm.getId()));
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 1.75, true, farm.getId()));

        MvcResult createdResult = mockMvc.perform(post("/feedings")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.createRequestJson("animal-1", feedType.getId(), savedUser.getId().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value(feedType.getId()))
                .andExpect(jsonPath("$.date").value("2026-03-24"))
                .andReturn();

        String feedingId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/feedings/{id}", feedingId)
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedingId))
                .andExpect(jsonPath("$.quantity").value(8.5))
                .andExpect(jsonPath("$.animal.id").value("animal-1"))
                .andExpect(jsonPath("$.animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$.feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$.feedType.name").value("Corn Silage"));

        mockMvc.perform(get("/feedings")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(feedingId))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].feedTypeId").value(feedType.getId()))
                .andExpect(jsonPath("$[0].animal.id").value("animal-1"))
                .andExpect(jsonPath("$[0].animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$[0].feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$[0].feedType.name").value("Corn Silage"));
    }

    @Test
    void shouldUpdateAndSoftDeleteFeedingThroughRealSpringContext() throws Exception {
        UserEntity savedUser = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(savedUser, "North Dairy");
        String authorization = bearerToken(savedUser);
        animalRepository.save(AnimalFixture.animalEntity("animal-1", "TAG-001", "Angus", java.time.LocalDate.of(2022, 1, 10), "ACTIVE", farm.getId()));
        animalRepository.save(AnimalFixture.animalEntity("animal-2", "TAG-002", "Holstein", java.time.LocalDate.of(2024, 1, 1), "ACTIVE", farm.getId()));
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 1.75, true, farm.getId()));
        FeedTypeEntity secondFeedType = feedTypeRepository.save(new FeedTypeEntity(null, "Hay", 2.0, true, farm.getId()));

        MvcResult createdResult = mockMvc.perform(post("/feedings")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.createRequestJson("animal-1", feedType.getId(), savedUser.getId().toString())))
                .andExpect(status().isCreated())
                .andReturn();

        String feedingId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(put("/feedings/{id}", feedingId)
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.updateRequestJson("animal-2", secondFeedType.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalId").value("animal-2"))
                .andExpect(jsonPath("$.feedTypeId").value(secondFeedType.getId()))
                .andExpect(jsonPath("$.date").value("2026-03-25"))
                .andExpect(jsonPath("$.quantity").value(10.0));

        mockMvc.perform(delete("/feedings/{id}", feedingId)
                        .header("Authorization", authorization))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/feedings/{id}", feedingId)
                        .header("Authorization", authorization))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/feedings")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldIgnoreProvidedCreateDateForWorker() throws Exception {
        UserEntity worker = createAuthenticatedUser("WORKER");
        FarmEntity farm = createFarmOwnedBy(worker, "North Dairy");
        String authorization = bearerToken(worker);
        animalRepository.save(AnimalFixture.animalEntity("animal-1", "TAG-001", "Angus", java.time.LocalDate.of(2022, 1, 10), "ACTIVE", farm.getId()));
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 1.75, true, farm.getId()));

        mockMvc.perform(post("/feedings")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.createRequestJson("animal-1", feedType.getId(), worker.getId().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value(java.time.LocalDate.now().toString()));
    }
}
