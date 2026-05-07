package com.jpsoftware.farmapp.integration.farm;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

class FarmOnboardingAccessIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private EmailSender emailSender;

    @Test
    void shouldRestrictOperationalEndpointsUntilUserCreatesFirstFarm() throws Exception {
        UserEntity user = createAuthenticatedUser("MANAGER");
        String authorization = bearerToken(user);

        mockMvc.perform(get("/animals")
                        .header("Authorization", authorization))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Crie uma fazenda antes de acessar este recurso."))
                .andExpect(jsonPath("$.path").value("/animals"));

        mockMvc.perform(get("/farms")
                        .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/farms")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Fazenda Inicial"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fazenda Inicial"));

        mockMvc.perform(get("/animals")
                        .header("Authorization", authorization))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPasswordUpdateBeforeFirstFarmExists() throws Exception {
        UserEntity user = createAuthenticatedUser("WORKER");

        mockMvc.perform(put("/users/me/password")
                        .header("Authorization", bearerToken(user))
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
                                  "password": "farmapp@456"
                                }
                                """.formatted(user.getEmail())))
                .andExpect(status().isOk());
    }
}
