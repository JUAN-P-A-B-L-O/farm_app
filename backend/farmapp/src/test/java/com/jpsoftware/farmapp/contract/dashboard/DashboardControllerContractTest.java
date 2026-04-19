package com.jpsoftware.farmapp.contract.dashboard;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.dashboard.controller.DashboardController;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerContractTest {

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
    void shouldReturnProfitSummary() throws Exception {
        when(dashboardService.getDashboard(null, true))
                .thenReturn(new DashboardResponse(100.0, 40.0, 500.0, 410.0, 12L));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduction").value(100.0))
                .andExpect(jsonPath("$.totalFeedingCost").value(40.0))
                .andExpect(jsonPath("$.totalRevenue").value(500.0))
                .andExpect(jsonPath("$.totalProfit").value(410.0))
                .andExpect(jsonPath("$.animalCount").value(12));
    }
}
