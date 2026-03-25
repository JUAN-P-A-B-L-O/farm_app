package com.jpsoftware.farmapp.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.exception.GlobalExceptionHandler;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerTest {

    private MockMvc mockMvc;
    private TestDashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new TestDashboardService();
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnDashboard() throws Exception {
        dashboardService.response = new DashboardResponse(100.0, 40.0, 200.0, 160.0, 12L);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduction").value(100.0))
                .andExpect(jsonPath("$.totalFeedingCost").value(40.0))
                .andExpect(jsonPath("$.totalRevenue").value(200.0))
                .andExpect(jsonPath("$.totalProfit").value(160.0))
                .andExpect(jsonPath("$.animalCount").value(12));
    }

    private static class TestDashboardService extends DashboardService {

        private DashboardResponse response;

        TestDashboardService() {
            super(dummyProductionRepository(), dummyFeedingRepository(), dummyAnimalRepository());
        }

        @Override
        public DashboardResponse getDashboard() {
            return response;
        }

        private static ProductionRepository dummyProductionRepository() {
            return (ProductionRepository) Proxy.newProxyInstance(
                    ProductionRepository.class.getClassLoader(),
                    new Class<?>[]{ProductionRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static FeedingRepository dummyFeedingRepository() {
            return (FeedingRepository) Proxy.newProxyInstance(
                    FeedingRepository.class.getClassLoader(),
                    new Class<?>[]{FeedingRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }

        private static AnimalRepository dummyAnimalRepository() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException("Repository should not be used in controller test");
                    });
        }
    }
}
