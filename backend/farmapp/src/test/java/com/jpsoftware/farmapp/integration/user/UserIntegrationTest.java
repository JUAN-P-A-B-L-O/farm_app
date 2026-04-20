package com.jpsoftware.farmapp.integration.user;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class UserIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldAllowManagerToCreateUserAndGrantAccessToAssignedFarm() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        FarmEntity farm = createFarmOwnedBy(manager, "North Dairy");

        mockMvc.perform(post("/users")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Worker One",
                                  "email": "worker.one@farm.com",
                                  "role": "WORKER",
                                  "password": "farmapp@123",
                                  "active": true,
                                  "farmIds": ["%s"]
                                }
                                """.formatted(farm.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Worker One"))
                .andExpect(jsonPath("$.email").value("worker.one@farm.com"))
                .andExpect(jsonPath("$.role").value("WORKER"));

        UserEntity createdUser = userRepository.findByEmail("worker.one@farm.com").orElseThrow();
        assertTrue(createdUser.isActive());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "worker.one@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/farms")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(farm.getId()))
                .andExpect(jsonPath("$[0].name").value("North Dairy"));

        mockMvc.perform(post("/animals")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tag": "COW-901",
                                  "breed": "Holstein",
                                  "birthDate": "2022-01-15",
                                  "origin": "BORN",
                                  "farmId": "%s"
                                }
                                """.formatted(farm.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.farmId").value(farm.getId()));
    }

    @Test
    void shouldRejectUserCreationFromNonManager() throws Exception {
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(post("/users")
                        .header("Authorization", bearerToken(worker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Worker Two",
                                  "email": "worker.two@farm.com",
                                  "role": "WORKER",
                                  "password": "farmapp@123",
                                  "active": true,
                                  "farmIds": ["farm-1"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDuplicateEmailDuringUserCreation() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        FarmEntity farm = createFarmOwnedBy(manager, "North Dairy");

        UserEntity existingUser = new UserEntity();
        existingUser.setName("Existing Worker");
        existingUser.setEmail("worker.one@farm.com");
        existingUser.setRole("WORKER");
        existingUser.setPassword(passwordEncoder.encode("farmapp@123"));
        existingUser.setActive(true);
        userRepository.save(existingUser);

        mockMvc.perform(post("/users")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Worker One",
                                  "email": "worker.one@farm.com",
                                  "role": "WORKER",
                                  "password": "farmapp@123",
                                  "active": true,
                                  "farmIds": ["%s"]
                                }
                                """.formatted(farm.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with this email already exists"));
    }

    @Test
    void shouldRequirePasswordForActiveUserCreation() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        FarmEntity farm = createFarmOwnedBy(manager, "North Dairy");

        mockMvc.perform(post("/users")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Worker Three",
                                  "email": "worker.three@farm.com",
                                  "role": "WORKER",
                                  "active": true,
                                  "farmIds": ["%s"]
                                }
                                """.formatted(farm.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("password must not be blank when active is true"));
    }

    @Test
    void shouldRequireCreatorOwnedFarmsDuringUserCreation() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity anotherManager = createAuthenticatedUser("MANAGER");
        FarmEntity otherFarm = createFarmOwnedBy(anotherManager, "Other Farm");

        mockMvc.perform(post("/users")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Worker Four",
                                  "email": "worker.four@farm.com",
                                  "role": "WORKER",
                                  "password": "farmapp@123",
                                  "active": true,
                                  "farmIds": ["%s"]
                                }
                                """.formatted(otherFarm.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("farmIds must reference farms owned by the authenticated manager"));
    }
}
