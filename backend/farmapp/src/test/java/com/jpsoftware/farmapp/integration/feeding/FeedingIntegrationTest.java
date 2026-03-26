package com.jpsoftware.farmapp.integration.feeding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.fixture.AnimalFixture;
import com.jpsoftware.farmapp.fixture.FeedingFixture;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class FeedingIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldReturnFeedingWithAnimalAndFeedTypeSummary() throws Exception {
        UserEntity savedUser = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "ADMIN"));
        animalRepository.save(AnimalFixture.animalEntity());
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 1.75, true));

        MvcResult createdResult = mockMvc.perform(post("/feedings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.createRequestJson("animal-1", feedType.getId(), savedUser.getId().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value(feedType.getId()))
                .andExpect(jsonPath("$.animal.id").value("animal-1"))
                .andExpect(jsonPath("$.animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$.feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$.feedType.name").value("Corn Silage"))
                .andReturn();

        String feedingId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/feedings/{id}", feedingId))
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
        UserEntity savedUser = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "ADMIN"));
        animalRepository.save(AnimalFixture.animalEntity());
        FeedTypeEntity feedType = feedTypeRepository.save(new FeedTypeEntity(null, "Corn Silage", 1.75, true));

        MvcResult createdResult = mockMvc.perform(post("/feedings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FeedingFixture.createRequestJson("animal-1", feedType.getId(), savedUser.getId().toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.feedTypeId").value(feedType.getId()))
                .andReturn();

        String feedingId = objectMapper.readTree(createdResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/feedings/{id}", feedingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedingId))
                .andExpect(jsonPath("$.quantity").value(8.5))
                .andExpect(jsonPath("$.animal.id").value("animal-1"))
                .andExpect(jsonPath("$.animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$.feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$.feedType.name").value("Corn Silage"));

        mockMvc.perform(get("/feedings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(feedingId))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].feedTypeId").value(feedType.getId()))
                .andExpect(jsonPath("$[0].animal.id").value("animal-1"))
                .andExpect(jsonPath("$[0].animal.tag").value("TAG-001"))
                .andExpect(jsonPath("$[0].feedType.id").value(feedType.getId()))
                .andExpect(jsonPath("$[0].feedType.name").value("Corn Silage"));
    }
}
