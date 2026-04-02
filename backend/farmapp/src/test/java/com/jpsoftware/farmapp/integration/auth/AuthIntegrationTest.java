package com.jpsoftware.farmapp.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                "ADMIN",
                passwordEncoder.encode("farmapp@123")));

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
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void shouldReturn401ForInvalidCredentials() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "ADMIN",
                passwordEncoder.encode("farmapp@123")));

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
    void shouldRequireTokenForProtectedEndpoints() throws Exception {
        UserEntity user = createAuthenticatedUser();

        mockMvc.perform(get("/animals"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/animals")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk());
    }
}
