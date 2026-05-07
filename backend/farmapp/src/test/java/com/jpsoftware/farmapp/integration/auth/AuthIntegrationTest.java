package com.jpsoftware.farmapp.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldRegisterAccountAndAllowLogin() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@farm.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.farmIds").isArray())
                .andExpect(jsonPath("$.farmIds").isEmpty())
                .andReturn();

        UserEntity registeredUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
        Assertions.assertTrue(registeredUser.isActive());
        Assertions.assertEquals("MANAGER", registeredUser.getRole());
        Assertions.assertTrue(passwordEncoder.matches("farmapp@123", registeredUser.getPassword()));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "maria@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.id").value(
                        objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("id").asText()))
                .andExpect(jsonPath("$.user.role").value("MANAGER"));
    }

    @Test
    void shouldRejectDuplicateEmailDuringRegistration() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Existing User",
                "maria@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
    }

    @Test
    void shouldRejectDuplicateEmailDuringRegistrationIgnoringCaseAndWhitespace() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Existing User",
                "maria@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "  MARIA@FARM.COM  ",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
    }

    @Test
    void shouldNormalizeRegistrationFieldsBeforePersistingUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Maria Silva  ",
                                  "email": "  MARIA@FARM.COM  ",
                                  "password": "  farmapp@123  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@farm.com"));

        UserEntity registeredUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
        Assertions.assertEquals("Maria Silva", registeredUser.getName());
        Assertions.assertTrue(passwordEncoder.matches("farmapp@123", registeredUser.getPassword()));
    }

    @Test
    void shouldValidateRequiredFieldsDuringRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@farm.com",
                                  "password": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A senha é obrigatória."))
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

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
                .andExpect(jsonPath("$.error").value("E-mail ou senha inválidos."));
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
                .andExpect(jsonPath("$.error").value("E-mail ou senha inválidos."));
    }

    @Test
    void shouldRequireTokenForProtectedEndpoints() throws Exception {
        UserEntity user = createAuthenticatedUser();

        mockMvc.perform(get("/animals"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("É necessário estar autenticado para acessar este recurso."))
                .andExpect(jsonPath("$.path").value("/animals"));

        mockMvc.perform(get("/animals/export"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("É necessário estar autenticado para acessar este recurso."))
                .andExpect(jsonPath("$.path").value("/animals/export"));

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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado."))
                .andExpect(jsonPath("$.path").value("/dashboard"));

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado."))
                .andExpect(jsonPath("$.path").value("/analytics/production"));

        mockMvc.perform(get("/dashboard/export")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado."))
                .andExpect(jsonPath("$.path").value("/dashboard/export"));

        mockMvc.perform(get("/analytics/production/export")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado."))
                .andExpect(jsonPath("$.path").value("/analytics/production/export"));

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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acesso negado."))
                .andExpect(jsonPath("$.path").value("/animals/missing"));

        mockMvc.perform(delete("/animals/missing")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldLocalizeDefaultErrorAttributesForMissingProtectedRoutes() throws Exception {
        UserEntity worker = createAuthenticatedUser("WORKER");

        mockMvc.perform(get("/missing-route")
                        .header("Authorization", bearerToken(worker)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso não encontrado."))
                .andExpect(jsonPath("$.path").value("/missing-route"));
    }

    @Test
    void shouldRejectTokenWithNonUuidSubjectOnProtectedEndpoint() throws Exception {
        String token = buildTokenWithSubject("not-a-uuid");

        mockMvc.perform(get("/animals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Token inválido ou expirado."))
                .andExpect(jsonPath("$.path").value("/animals"));
    }

    private String buildTokenWithSubject(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(
                        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                                .getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
