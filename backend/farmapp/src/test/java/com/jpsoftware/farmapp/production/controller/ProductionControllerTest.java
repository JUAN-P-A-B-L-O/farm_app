package com.jpsoftware.farmapp.production.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.service.ProductionService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductionController.class)
@Import(GlobalExceptionHandler.class)
class ProductionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductionService productionService;

    @Test
    void shouldCreateProduction() throws Exception {
        String requestBody = """
                {
                  "animalId": "animal-1",
                  "date": "2026-03-20",
                  "quantity": 12.5
                }
                """;

        when(productionService.create(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/productions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("production-1"))
                .andExpect(jsonPath("$.animalId").value("animal-1"))
                .andExpect(jsonPath("$.quantity").value(12.5));

        verify(productionService).create(any());
    }

    @Test
    void shouldReturn400WhenInvalid() throws Exception {
        String requestBody = """
                {
                  "animalId": "animal-1",
                  "date": "2026-03-20",
                  "quantity": 0
                }
                """;

        mockMvc.perform(post("/productions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    private ProductionResponse buildResponse() {
        return new ProductionResponse(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5);
    }
}
