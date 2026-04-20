package com.jpsoftware.farmapp.integration.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                                  "avatarUrl": "https://example.com/avatar.png",
                                  "farmIds": ["%s"]
                                }
                                """.formatted(farm.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Worker One"))
                .andExpect(jsonPath("$.email").value("worker.one@farm.com"))
                .andExpect(jsonPath("$.role").value("WORKER"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"));

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
                                  "avatarUrl": "https://example.com/avatar.png",
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
                                  "avatarUrl": "https://example.com/avatar.png",
                                  "farmIds": ["%s"]
                                }
                                """.formatted(otherFarm.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("farmIds must reference farms owned by the authenticated manager"));
    }

    @Test
    void shouldAllowManagerToUpdateUser() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        FarmEntity farm = createFarmOwnedBy(manager, "North Dairy");
        UserEntity worker = createAuthenticatedUser("WORKER");

        userFarmAssignmentRepository.save(new com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity(null, worker.getId(), farm.getId()));

        mockMvc.perform(put("/users/" + worker.getId())
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Worker",
                                  "email": "updated.worker@farm.com",
                                  "role": "WORKER",
                                  "avatarUrl": "https://example.com/avatar-updated.png",
                                  "farmIds": ["%s"]
                                }
                                """.formatted(farm.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Worker"))
                .andExpect(jsonPath("$.email").value("updated.worker@farm.com"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar-updated.png"))
                .andExpect(jsonPath("$.farmIds[0]").value(farm.getId()));

        UserEntity updatedUser = userRepository.findById(worker.getId()).orElseThrow();
        assertEquals("Updated Worker", updatedUser.getName());
        assertEquals("updated.worker@farm.com", updatedUser.getEmail());
    }

    @Test
    void shouldAllowManagerToInactivateUserAndBlockLogin() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(patch("/users/" + worker.getId() + "/inactivate")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        UserEntity updatedUser = userRepository.findById(worker.getId()).orElseThrow();
        assertFalse(updatedUser.isActive());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "farmapp@123"
                                }
                                """.formatted(worker.getEmail())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowManagerToReactivateUserAndRestoreLogin() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity worker = createAuthenticatedUser("WORKER");
        worker.setActive(false);
        userRepository.save(worker);

        mockMvc.perform(patch("/users/" + worker.getId() + "/activate")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "farmapp@456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        UserEntity updatedUser = userRepository.findById(worker.getId()).orElseThrow();
        assertTrue(updatedUser.isActive());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "farmapp@456"
                                }
                                """.formatted(worker.getEmail())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowManagerToDeleteUser() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(delete("/users/" + worker.getId())
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(worker.getId()).isEmpty());
    }

    @Test
    void shouldRejectDeletingUserWhoOwnsFarms() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity owner = createAuthenticatedUser("MANAGER");
        createFarmOwnedBy(owner, "Owner Farm");

        mockMvc.perform(delete("/users/" + owner.getId())
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete user who owns farms"));
    }

    @Test
    void shouldAllowAuthenticatedUserToUpdateOwnPassword() throws Exception {
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(put("/users/me/password")
                        .header("Authorization", bearerToken(worker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "farmapp@123",
                                  "newPassword": "farmapp@456"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "farmapp@123"
                                }
                                """.formatted(worker.getEmail())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "farmapp@456"
                                }
                                """.formatted(worker.getEmail())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterUsersBySearchStatusAndRole() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity inactiveWorker = createAuthenticatedUser("WORKER");
        inactiveWorker.setName("Pedro Worker");
        inactiveWorker.setEmail("pedro.worker@farm.com");
        inactiveWorker.setActive(false);
        userRepository.save(inactiveWorker);

        mockMvc.perform(get("/users")
                        .header("Authorization", bearerToken(manager))
                        .param("search", "pedro")
                        .param("active", "false")
                        .param("role", "worker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("pedro.worker@farm.com"))
                .andExpect(jsonPath("$[0].active").value(false))
                .andExpect(jsonPath("$[0].role").value("WORKER"));
    }

    @Test
    void shouldRejectUserLifecycleManagementFromNonManager() throws Exception {
        UserEntity worker = createAuthenticatedUser("WORKER");
        UserEntity target = createAuthenticatedUser("WORKER");

        mockMvc.perform(put("/users/" + target.getId())
                        .header("Authorization", bearerToken(worker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Worker",
                                  "email": "updated.worker@farm.com",
                                  "role": "WORKER",
                                  "farmIds": ["farm-1"]
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/users/" + target.getId() + "/inactivate")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/users/" + target.getId() + "/activate")
                        .header("Authorization", bearerToken(worker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "farmapp@456"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/users/" + target.getId())
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());
    }
}
