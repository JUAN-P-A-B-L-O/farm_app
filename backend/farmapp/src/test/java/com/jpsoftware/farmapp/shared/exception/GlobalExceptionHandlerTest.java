package com.jpsoftware.farmapp.shared.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.shared.onboarding.FarmOnboardingRequiredException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestExceptionController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturn409WhenDuplicateResource() throws Exception {
        mockMvc.perform(get("/test-exceptions/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Já existe um animal com esta tag."))
                .andExpect(jsonPath("$.path").value("/test-exceptions/conflict"));
    }

    @Test
    void shouldReturn400OnValidationError() throws Exception {
        mockMvc.perform(post("/test-exceptions/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("O nome é obrigatório."))
                .andExpect(jsonPath("$.path").value("/test-exceptions/validation"));
    }

    @Test
    void shouldReturn404WhenResourceNotFound() throws Exception {
        mockMvc.perform(get("/test-exceptions/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso não encontrado."))
                .andExpect(jsonPath("$.path").value("/test-exceptions/not-found"));
    }

    @Test
    void shouldReturnLocalized404WhenStaticResourceDoesNotExist() throws Exception {
        mockMvc.perform(get("/test-exceptions/missing-static-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso não encontrado."))
                .andExpect(jsonPath("$.path").value("/test-exceptions/missing-static-resource"));
    }

    @Test
    void shouldReturn403WhenFarmOnboardingIsRequired() throws Exception {
        mockMvc.perform(get("/test-exceptions/onboarding-required"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Crie uma fazenda antes de acessar este recurso."))
                .andExpect(jsonPath("$.path").value("/test-exceptions/onboarding-required"));
    }

    @RestController
    @RequestMapping("/test-exceptions")
    private static class TestExceptionController {

        @GetMapping("/conflict")
        ResponseEntity<Void> conflict() {
            throw new DataIntegrityViolationException("duplicate tag constraint violation");
        }

        @PostMapping("/validation")
        ResponseEntity<Void> validation(@Valid @RequestBody ValidationRequest request) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/not-found")
        ResponseEntity<Void> notFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/missing-static-resource")
        ResponseEntity<Void> missingStaticResource() throws NoResourceFoundException {
            throw new NoResourceFoundException(null, "/test-exceptions/missing-static-resource");
        }

        @GetMapping("/onboarding-required")
        ResponseEntity<Void> onboardingRequired() {
            throw new FarmOnboardingRequiredException("Create a farm before accessing this feature");
        }
    }

    private record ValidationRequest(
            @NotBlank(message = "name must not be blank") String name) {
    }
}
