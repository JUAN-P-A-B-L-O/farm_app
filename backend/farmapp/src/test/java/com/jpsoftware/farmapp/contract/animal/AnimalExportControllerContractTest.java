package com.jpsoftware.farmapp.contract.animal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.controller.AnimalController;
import com.jpsoftware.farmapp.animal.service.AnimalService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AnimalExportControllerContractTest {

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
    void shouldExportAnimalsAsCsv() throws Exception {
        when(animalService.exportAll("farm-001")).thenReturn("id,tag\nanimal-1,TAG-001\n");

        mockMvc.perform(get("/animals/export").param("farmId", "farm-001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("animals.csv")))
                .andExpect(content().string("id,tag\nanimal-1,TAG-001\n"));

        verify(animalService).exportAll("farm-001");
    }
}
