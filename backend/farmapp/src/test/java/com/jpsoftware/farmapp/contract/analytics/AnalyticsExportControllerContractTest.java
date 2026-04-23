package com.jpsoftware.farmapp.contract.analytics;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.analytics.controller.AnalyticsController;
import com.jpsoftware.farmapp.analytics.service.AnalyticsService;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AnalyticsExportControllerContractTest {

    private MockMvc mockMvc;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = org.mockito.Mockito.mock(AnalyticsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalyticsController(analyticsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldExportProductionAnalyticsAsCsv() throws Exception {
        when(analyticsService.exportProductionSeries(null, null, "animal-1", "month", "farm-1"))
                .thenReturn("period,value\n2026-04,10.0\n");

        mockMvc.perform(get("/analytics/production/export")
                        .param("animalId", "animal-1")
                        .param("groupBy", "month")
                        .param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("analytics-production.csv")))
                .andExpect(content().string("period,value\n2026-04,10.0\n"));

        verify(analyticsService).exportProductionSeries(null, null, "animal-1", "month", "farm-1");
    }

    @Test
    void shouldExportFeedingAnalyticsAsCsv() throws Exception {
        when(analyticsService.exportFeedingCostSeries(null, null, "animal-1", "day", "farm-1"))
                .thenReturn("period,value\n2026-04-14,20.0\n");

        mockMvc.perform(get("/analytics/feeding/export")
                        .param("animalId", "animal-1")
                        .param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("analytics-feeding.csv")))
                .andExpect(content().string("period,value\n2026-04-14,20.0\n"));

        verify(analyticsService).exportFeedingCostSeries(null, null, "animal-1", "day", "farm-1");
    }

    @Test
    void shouldExportProfitAnalyticsAsCsv() throws Exception {
        when(analyticsService.exportProfitSeries(null, null, "animal-1", "day", "farm-1", false))
                .thenReturn("period,profit\n2026-04-14,270.0\n");

        mockMvc.perform(get("/analytics/profit/export")
                        .param("animalId", "animal-1")
                        .param("farmId", "farm-1")
                        .param("includeAcquisitionCost", "false"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("analytics-profit.csv")))
                .andExpect(content().string("period,profit\n2026-04-14,270.0\n"));

        verify(analyticsService).exportProfitSeries(null, null, "animal-1", "day", "farm-1", false);
    }

    @Test
    void shouldExportProductionByAnimalAsCsv() throws Exception {
        when(analyticsService.exportProductionByAnimal(null, null, "animal-1", "farm-1"))
                .thenReturn("animalId,animalTag,quantity\nanimal-1,TAG-001,10.0\n");

        mockMvc.perform(get("/analytics/production/by-animal/export")
                        .param("animalId", "animal-1")
                        .param("farmId", "farm-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", Matchers.containsString("analytics-production-by-animal.csv")))
                .andExpect(content().string("animalId,animalTag,quantity\nanimal-1,TAG-001,10.0\n"));

        verify(analyticsService).exportProductionByAnimal(null, null, "animal-1", "farm-1");
    }
}
