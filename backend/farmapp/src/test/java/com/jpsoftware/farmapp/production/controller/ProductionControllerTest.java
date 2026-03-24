package com.jpsoftware.farmapp.production.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.service.ProductionService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
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
    void shouldReturnAllProductions() throws Exception {
        when(productionService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/productions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("production-1"))
                .andExpect(jsonPath("$[0].animalId").value("animal-1"))
                .andExpect(jsonPath("$[0].quantity").value(12.5));

        verify(productionService).findAll();
    }

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

    @Test
    void shouldUpdateProduction() throws Exception {
        String requestBody = """
                {
                  "date": "2026-03-21",
                  "quantity": 15.0
                }
                """;

        when(productionService.update(any(), any())).thenReturn(new ProductionResponse(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 21),
                15.0));

        mockMvc.perform(put("/productions/production-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("production-1"))
                .andExpect(jsonPath("$.date").value("2026-03-21"))
                .andExpect(jsonPath("$.quantity").value(15.0));

        verify(productionService).update(any(), any());
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        String requestBody = """
                {
                  "quantity": 15.0
                }
                """;

        when(productionService.update(any(), any()))
                .thenThrow(new ResourceNotFoundException("Production not found"));

        mockMvc.perform(put("/productions/missing-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Production not found"))
                .andExpect(jsonPath("$.path").value("/productions/missing-id"));
    }

    private ProductionResponse buildResponse() {
        return new ProductionResponse(
                "production-1",
                "animal-1",
                LocalDate.of(2026, 3, 20),
                12.5);
    }
}
