package com.jpsoftware.farmapp.animal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.service.AnimalService;
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

@WebMvcTest(AnimalController.class)
@Import(GlobalExceptionHandler.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnimalService animalService;

    @Test
    void shouldCreateAnimal() throws Exception {
        AnimalResponse response = buildResponse();
        String requestBody = """
                {
                  "tag": "TAG-001",
                  "breed": "Angus",
                  "birthDate": "2022-01-10",
                  "farmId": "FARM-001"
                }
                """;

        when(animalService.create(any())).thenReturn(response);

        mockMvc.perform(post("/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("animal-1"))
                .andExpect(jsonPath("$.tag").value("TAG-001"));

        verify(animalService).create(any());
    }

    @Test
    void shouldGetAllAnimals() throws Exception {
        when(animalService.findAll(null)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("animal-1"))
                .andExpect(jsonPath("$[0].breed").value("Angus"));

        verify(animalService).findAll(null);
    }

    @Test
    void shouldGetAnimalById() throws Exception {
        when(animalService.findById("animal-1")).thenReturn(buildResponse());

        mockMvc.perform(get("/animals/animal-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("animal-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(animalService).findById("animal-1");
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        when(animalService.findById("missing-id"))
                .thenThrow(new ResourceNotFoundException("Animal not found"));

        mockMvc.perform(get("/animals/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Animal not found"))
                .andExpect(jsonPath("$.path").value("/animals/missing-id"));

        verify(animalService).findById("missing-id");
    }

    @Test
    void shouldUpdateAnimal() throws Exception {
        String requestBody = """
                {
                  "tag": "TAG-002",
                  "breed": "Nelore",
                  "birthDate": "2021-05-20",
                  "status": "INACTIVE",
                  "farmId": "FARM-002"
                }
                """;
        AnimalResponse response = AnimalResponse.builder()
                .id("animal-1")
                .tag("TAG-002")
                .breed("Nelore")
                .birthDate(LocalDate.of(2021, 5, 20))
                .status("INACTIVE")
                .farmId("FARM-002")
                .build();

        when(animalService.update(eq("animal-1"), any())).thenReturn(response);

        mockMvc.perform(put("/animals/animal-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tag").value("TAG-002"))
                .andExpect(jsonPath("$.farmId").value("FARM-002"));

        verify(animalService).update(eq("animal-1"), any());
    }

    @Test
    void shouldDeleteAnimal() throws Exception {
        doNothing().when(animalService).delete("animal-1");

        mockMvc.perform(delete("/animals/animal-1"))
                .andExpect(status().isNoContent());

        verify(animalService).delete("animal-1");
    }

    private AnimalResponse buildResponse() {
        return AnimalResponse.builder()
                .id("animal-1")
                .tag("TAG-001")
                .breed("Angus")
                .birthDate(LocalDate.of(2022, 1, 10))
                .status("ACTIVE")
                .farmId("FARM-001")
                .build();
    }
}
