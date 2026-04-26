package com.jpsoftware.farmapp.contract.dashboard;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.dashboard.controller.DashboardController;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardExportControllerContractTest {

    private MockMvc mockMvc;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = org.mockito.Mockito.mock(DashboardService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldExportDashboardAsCsv() throws Exception {
        when(dashboardService.exportDashboard("farm-001", null, null, null, null, true))
                .thenReturn("totalProduction,totalFeedingCost\n100.0,50.0\n");

        mockMvc.perform(get("/dashboard/export")
                        .param("farmId", "farm-001")
                        .param("includeAcquisitionCost", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("dashboard-summary.csv")))
                .andExpect(content().string("totalProduction,totalFeedingCost\n100.0,50.0\n"));

        verify(dashboardService).exportDashboard("farm-001", null, null, null, null, true);
    }

    @Test
    void shouldExportDashboardAsCsvWithCurrencyContext() throws Exception {
        when(dashboardService.exportDashboard(
                "farm-001",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-28"),
                "animal-9",
                "SOLD",
                true,
                "USD"))
                .thenReturn("totalProduction,totalFeedingCost\n100.0,10.0\n");

        mockMvc.perform(get("/dashboard/export")
                        .param("farmId", "farm-001")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-28")
                        .param("animalId", "animal-9")
                        .param("status", "SOLD")
                        .param("includeAcquisitionCost", "true")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("dashboard-summary.csv")))
                .andExpect(content().string("totalProduction,totalFeedingCost\n100.0,10.0\n"));

        verify(dashboardService).exportDashboard(
                "farm-001",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-28"),
                "animal-9",
                "SOLD",
                true,
                "USD");
    }

    @Test
    void shouldExportDashboardAsCsvWithMultiAnimalFilters() throws Exception {
        when(dashboardService.exportDashboardByAnimals(
                "farm-001",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-28"),
                List.of("animal-9", "animal-10"),
                "SOLD",
                true,
                "USD"))
                .thenReturn("totalProduction,totalFeedingCost\n140.0,12.0\n");

        mockMvc.perform(get("/dashboard/export")
                        .param("farmId", "farm-001")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-02-28")
                        .param("animalIds", "animal-9,animal-10")
                        .param("status", "SOLD")
                        .param("includeAcquisitionCost", "true")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("dashboard-summary.csv")))
                .andExpect(content().string("totalProduction,totalFeedingCost\n140.0,12.0\n"));

        verify(dashboardService).exportDashboardByAnimals(
                "farm-001",
                LocalDate.parse("2026-02-01"),
                LocalDate.parse("2026-02-28"),
                List.of("animal-9", "animal-10"),
                "SOLD",
                true,
                "USD");
    }
}
