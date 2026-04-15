package com.jpsoftware.farmapp.contract.animal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.controller.AnimalController;
import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.SellAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.service.AnimalService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AnimalControllerContractTest {

    private MockMvc mockMvc;
    private AnimalService animalService;

    @BeforeEach
    void setUp() {
        animalService = org.mockito.Mockito.mock(AnimalService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnimalController(animalService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateAnimalSuccessfully() throws Exception {
        when(animalService.create(org.mockito.ArgumentMatchers.any(CreateAnimalRequest.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tag": "TAG-001",
                                  "breed": "Angus",
                                  "birthDate": "2022-01-10",
                                  "origin": "BORN",
                                  "farmId": "FARM-001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("animal-1"))
                .andExpect(jsonPath("$.tag").value("TAG-001"));
    }

    @Test
    void shouldReturnAllAnimals() throws Exception {
        when(animalService.findAll(null)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("animal-1"));
    }

    @Test
    void shouldReturnAnimalById() throws Exception {
        when(animalService.findById("animal-1", null)).thenReturn(buildResponse());

        mockMvc.perform(get("/animals/animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldUpdateAnimalSuccessfully() throws Exception {
        when(animalService.update(eq("animal-1"), org.mockito.ArgumentMatchers.any(UpdateAnimalRequest.class), eq(null)))
                .thenReturn(AnimalResponse.builder()
                        .id("animal-1")
                        .tag("TAG-001")
                        .breed("Angus")
                        .birthDate(LocalDate.of(2022, 1, 10))
                        .status("INACTIVE")
                        .origin("BORN")
                        .farmId("FARM-001")
                        .build());

        mockMvc.perform(put("/animals/animal-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldSellAnimalSuccessfully() throws Exception {
        when(animalService.sell(eq("animal-1"), org.mockito.ArgumentMatchers.any(SellAnimalRequest.class), eq(null)))
                .thenReturn(AnimalResponse.builder()
                        .id("animal-1")
                        .tag("TAG-001")
                        .breed("Angus")
                        .birthDate(LocalDate.of(2022, 1, 10))
                        .status("SOLD")
                        .origin("BORN")
                        .salePrice(3200.0)
                        .saleDate(LocalDate.of(2026, 4, 14))
                        .farmId("FARM-001")
                        .build());

        mockMvc.perform(post("/animals/animal-1/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "salePrice": 3200.00,
                                  "saleDate": "2026-04-14"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"))
                .andExpect(jsonPath("$.salePrice").value(3200.0))
                .andExpect(jsonPath("$.saleDate").value("2026-04-14"));

        verify(animalService).sell(eq("animal-1"), org.mockito.ArgumentMatchers.any(SellAnimalRequest.class), eq(null));
    }

    @Test
    void shouldDeleteAnimalSuccessfully() throws Exception {
        mockMvc.perform(delete("/animals/animal-1"))
                .andExpect(status().isNoContent());

        verify(animalService).delete("animal-1", null);
    }

    private AnimalResponse buildResponse() {
        return AnimalResponse.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status("ACTIVE")
                .origin("BORN")
                .farmId("FARM-001")
                .build();
    }
}
