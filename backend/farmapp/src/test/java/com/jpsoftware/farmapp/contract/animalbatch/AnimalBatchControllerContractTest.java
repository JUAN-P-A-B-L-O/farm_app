package com.jpsoftware.farmapp.contract.animalbatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.animalbatch.controller.AnimalBatchController;
import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.dto.UpdateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.service.AnimalBatchService;
import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AnimalBatchControllerContractTest {

    private MockMvc mockMvc;
    private AnimalBatchService animalBatchService;

    @BeforeEach
    void setUp() {
        animalBatchService = org.mockito.Mockito.mock(AnimalBatchService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnimalBatchController(animalBatchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateAnimalBatchSuccessfully() throws Exception {
        when(animalBatchService.create(any(CreateAnimalBatchRequest.class), eq("farm-1"))).thenReturn(buildResponse());

        mockMvc.perform(post("/animal-batches")
                        .param("farmId", "farm-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lote A",
                                  "animalIds": ["animal-1", "animal-2"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("batch-1"))
                .andExpect(jsonPath("$.name").value("Lote A"))
                .andExpect(jsonPath("$.farmId").value("farm-1"))
                .andExpect(jsonPath("$.animals.length()").value(2));
    }

    @Test
    void shouldReturnPaginatedAnimalBatches() throws Exception {
        when(animalBatchService.findAllPaginated(null, "farm-1", 0, 10)).thenReturn(new PaginatedResponse<>(
                List.of(buildResponse()),
                0,
                10,
                1,
                1));

        mockMvc.perform(get("/animal-batches")
                        .param("farmId", "farm-1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("batch-1"))
                .andExpect(jsonPath("$.content[0].animals.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldReturnAnimalBatchById() throws Exception {
        when(animalBatchService.findById("batch-1", "farm-1")).thenReturn(buildResponse());

        mockMvc.perform(get("/animal-batches/batch-1").param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("batch-1"))
                .andExpect(jsonPath("$.name").value("Lote A"))
                .andExpect(jsonPath("$.animals[0].tag").value("TAG-001"));
    }

    @Test
    void shouldUpdateAnimalBatchSuccessfully() throws Exception {
        when(animalBatchService.update(eq("batch-1"), any(UpdateAnimalBatchRequest.class), eq("farm-1")))
                .thenReturn(new AnimalBatchResponse(
                        "batch-1",
                        "Lote B",
                        "farm-1",
                        List.of(new AnimalSummaryResponse("animal-2", "TAG-002"))));

        mockMvc.perform(put("/animal-batches/batch-1")
                        .param("farmId", "farm-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lote B",
                                  "animalIds": ["animal-2"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lote B"))
                .andExpect(jsonPath("$.animals.length()").value(1))
                .andExpect(jsonPath("$.animals[0].id").value("animal-2"));
    }

    @Test
    void shouldDeleteAnimalBatchSuccessfully() throws Exception {
        mockMvc.perform(delete("/animal-batches/batch-1").param("farmId", "farm-1"))
                .andExpect(status().isNoContent());

        verify(animalBatchService).delete("batch-1", "farm-1");
    }

    @Test
    void shouldFailWhenAnimalIdsAreMissing() throws Exception {
        mockMvc.perform(post("/animal-batches")
                        .param("farmId", "farm-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Lote inválido",
                                  "animalIds": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Selecione ao menos um animal."))
                .andExpect(jsonPath("$.path").value("/animal-batches"));
    }

    @Test
    void shouldFailWhenBatchIsNotFound() throws Exception {
        when(animalBatchService.findById("missing-batch", "farm-1"))
                .thenThrow(new ResourceNotFoundException("Batch not found"));

        mockMvc.perform(get("/animal-batches/missing-batch").param("farmId", "farm-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Lote não encontrado."))
                .andExpect(jsonPath("$.path").value("/animal-batches/missing-batch"));
    }

    private AnimalBatchResponse buildResponse() {
        return new AnimalBatchResponse(
                "batch-1",
                "Lote A",
                "farm-1",
                List.of(
                        new AnimalSummaryResponse("animal-1", "TAG-001"),
                        new AnimalSummaryResponse("animal-2", "TAG-002")));
    }
}
