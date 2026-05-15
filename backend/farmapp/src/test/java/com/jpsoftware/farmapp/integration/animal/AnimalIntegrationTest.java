package com.jpsoftware.farmapp.integration.animal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.dao.DataIntegrityViolationException;
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
                .andExpect(jsonPath("$.error").value("Já existe um animal com esta tag nesta fazenda."));
    }

    @Test
    void shouldAllowSameTagInDifferentFarms() throws Exception {
        UserEntity user = createAuthenticatedUser();
        FarmEntity firstFarm = createFarmOwnedBy(user, "North Dairy");
        FarmEntity secondFarm = createFarmOwnedBy(user, "South Dairy");
        String authorization = bearerToken(user);

        mockMvc.perform(post("/animals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.createRequestJson(firstFarm.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/animals")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AnimalFixture.createRequestJson(secondFarm.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tag").value("TAG-001"))
                .andExpect(jsonPath("$.farmId").value(secondFarm.getId()));
    }

    @Test
    void shouldEnforceCompositeTagUniquenessAtDatabaseLevel() {
        UserEntity user = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(user, "North Dairy");

        animalRepository.saveAndFlush(AnimalEntity.builder()
                .id("animal-first")
                .tag("TAG-001")
                .breed("Holstein")
                .birthDate(java.time.LocalDate.of(2023, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(farm.getId())
                .build());

        assertThrows(
                DataIntegrityViolationException.class,
                () -> animalRepository.saveAndFlush(AnimalEntity.builder()
                        .id("animal-second")
                        .tag("TAG-001")
                        .breed("Jersey")
                        .birthDate(java.time.LocalDate.of(2023, 2, 12))
                        .status(AnimalEntity.STATUS_ACTIVE)
                        .origin(AnimalEntity.ORIGIN_BORN)
                        .farmId(farm.getId())
                        .build()));
    }

    @Test
    void shouldFailWhenMovingAnimalToFarmWithDuplicateTag() throws Exception {
        UserEntity user = createAuthenticatedUser();
        FarmEntity sourceFarm = createFarmOwnedBy(user, "North Dairy");
        FarmEntity destinationFarm = createFarmOwnedBy(user, "South Dairy");
        String authorization = bearerToken(user);

        AnimalEntity movedAnimal = animalRepository.save(AnimalEntity.builder()
                .id("animal-to-move")
                .tag("TAG-001")
                .breed("Holstein")
                .birthDate(java.time.LocalDate.of(2023, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(sourceFarm.getId())
                .build());
        animalRepository.save(AnimalEntity.builder()
                .id("animal-in-destination")
                .tag("TAG-001")
                .breed("Jersey")
                .birthDate(java.time.LocalDate.of(2023, 2, 12))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(destinationFarm.getId())
                .build());

        mockMvc.perform(put("/animals/{id}", movedAnimal.getId())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "farmId": "%s"
                                }
                                """.formatted(destinationFarm.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um animal com esta tag nesta fazenda."));
    }

    @Test
    void shouldFailWhenUpdatingTagToExistingTagInSameFarm() throws Exception {
        UserEntity user = createAuthenticatedUser();
        FarmEntity farm = createFarmOwnedBy(user, "North Dairy");
        String authorization = bearerToken(user);

        AnimalEntity firstAnimal = animalRepository.save(AnimalEntity.builder()
                .id("animal-first")
                .tag("TAG-001")
                .breed("Holstein")
                .birthDate(java.time.LocalDate.of(2023, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(farm.getId())
                .build());
        animalRepository.save(AnimalEntity.builder()
                .id("animal-second")
                .tag("TAG-002")
                .breed("Jersey")
                .birthDate(java.time.LocalDate.of(2023, 2, 12))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(farm.getId())
                .build());

        mockMvc.perform(put("/animals/{id}", firstAnimal.getId())
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tag": "TAG-002"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um animal com esta tag nesta fazenda."));
    }

    @Test
    void shouldRestrictAnimalReadsToAccessibleFarmsWhenFarmIdIsOmitted() throws Exception {
        UserEntity authorizedUser = createAuthenticatedUser();
        UserEntity otherUser = createAuthenticatedUser();
        FarmEntity authorizedFarm = createFarmOwnedBy(authorizedUser, "North Dairy");
        FarmEntity otherFarm = createFarmOwnedBy(otherUser, "South Dairy");
        AnimalEntity authorizedAnimal = animalRepository.save(AnimalEntity.builder()
                .id("animal-authorized")
                .tag("AUTH-001")
                .breed("Holstein")
                .birthDate(java.time.LocalDate.of(2023, 1, 10))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(authorizedFarm.getId())
                .build());
        AnimalEntity unauthorizedAnimal = animalRepository.save(AnimalEntity.builder()
                .id("animal-unauthorized")
                .tag("OTHER-001")
                .breed("Jersey")
                .birthDate(java.time.LocalDate.of(2023, 2, 12))
                .status(AnimalEntity.STATUS_ACTIVE)
                .origin(AnimalEntity.ORIGIN_BORN)
                .farmId(otherFarm.getId())
                .build());

        mockMvc.perform(get("/animals")
                        .header("Authorization", bearerToken(authorizedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(authorizedAnimal.getId()))
                .andExpect(jsonPath("$[0].farmId").value(authorizedFarm.getId()));

        mockMvc.perform(get("/animals/{id}", unauthorizedAnimal.getId())
                        .header("Authorization", bearerToken(authorizedUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Animal não encontrado."));
    }
}
