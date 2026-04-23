package com.jpsoftware.farmapp.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldLoginAndReturnJwtToken() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.email").value("jane@farm.com"))
                .andExpect(jsonPath("$.user.role").value("MANAGER"));
    }

    @Test
    void shouldReturn401ForInvalidCredentials() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void shouldReturn401ForInactiveUser() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "WORKER",
                passwordEncoder.encode("farmapp@123"),
                false));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    void shouldRequireTokenForProtectedEndpoints() throws Exception {
        UserEntity user = createAuthenticatedUser();

        mockMvc.perform(get("/animals"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/animals/export"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/animals")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/animals/export")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));
    }

    @Test
    void shouldAllowOnlyManagersToAccessDashboardAndAnalytics() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(get("/dashboard")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/export")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/analytics/production/export")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/export")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));

        mockMvc.perform(get("/analytics/production/export")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"));
    }

    @Test
    void shouldAllowOnlyManagersToReachDeleteEndpoints() throws Exception {
        UserEntity manager = createAuthenticatedUser("MANAGER");
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(delete("/animals/missing")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/animals/missing")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isNotFound());
    }
}
