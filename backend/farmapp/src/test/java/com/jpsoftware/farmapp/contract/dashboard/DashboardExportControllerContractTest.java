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
        when(dashboardService.exportDashboard("farm-001", true))
                .thenReturn("totalProduction,totalFeedingCost\n100.0,50.0\n");

        mockMvc.perform(get("/dashboard/export")
                        .param("farmId", "farm-001")
                        .param("includeAcquisitionCost", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("dashboard-summary.csv")))
                .andExpect(content().string("totalProduction,totalFeedingCost\n100.0,50.0\n"));

        verify(dashboardService).exportDashboard("farm-001", true);
    }

    @Test
    void shouldExportDashboardAsCsvWithCurrencyContext() throws Exception {
        when(dashboardService.exportDashboard("farm-001", true, "USD"))
                .thenReturn("totalProduction,totalFeedingCost\n100.0,10.0\n");

        mockMvc.perform(get("/dashboard/export")
                        .param("farmId", "farm-001")
                        .param("includeAcquisitionCost", "true")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("dashboard-summary.csv")))
                .andExpect(content().string("totalProduction,totalFeedingCost\n100.0,10.0\n"));

        verify(dashboardService).exportDashboard("farm-001", true, "USD");
    }
}
