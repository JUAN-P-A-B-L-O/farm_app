package com.jpsoftware.farmapp.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    private AggregateProductionRepository productionRepositoryHandler;
    private AggregateFeedingRepository feedingRepositoryHandler;
    private CountAnimalRepository animalRepositoryHandler;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        productionRepositoryHandler = new AggregateProductionRepository();
        feedingRepositoryHandler = new AggregateFeedingRepository();
        animalRepositoryHandler = new CountAnimalRepository();

        dashboardService = new DashboardService(
                productionRepositoryHandler.createProxy(),
                feedingRepositoryHandler.createProxy(),
                animalRepositoryHandler.createProxy());
    }

    @Test
    void shouldReturnDashboardData() {
        productionRepositoryHandler.totalProduction = 100.0;
        feedingRepositoryHandler.totalFeedingCost = 40.0;
        animalRepositoryHandler.count = 12L;

        DashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(100.0, response.getTotalProduction());
        assertEquals(40.0, response.getTotalFeedingCost());
        assertEquals(200.0, response.getTotalRevenue());
        assertEquals(160.0, response.getTotalProfit());
        assertEquals(12L, response.getAnimalCount());
    }

    @Test
    void shouldReturnZeroWhenNoData() {
        productionRepositoryHandler.totalProduction = null;
        feedingRepositoryHandler.totalFeedingCost = null;
        animalRepositoryHandler.count = 0L;

        DashboardResponse response = dashboardService.getDashboard();

        assertNotNull(response);
        assertEquals(0.0, response.getTotalProduction());
        assertEquals(0.0, response.getTotalFeedingCost());
        assertEquals(0.0, response.getTotalRevenue());
        assertEquals(0.0, response.getTotalProfit());
        assertEquals(0L, response.getAnimalCount());
    }

    private static class AggregateProductionRepository {

        private Double totalProduction;

        ProductionRepository createProxy() {
            return (ProductionRepository) Proxy.newProxyInstance(
                    ProductionRepository.class.getClassLoader(),
                    new Class<?>[]{ProductionRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("sumTotalProduction".equals(methodName)) {
                            return totalProduction;
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "AggregateProductionRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }
    }

    private static class AggregateFeedingRepository {

        private Double totalFeedingCost;

        FeedingRepository createProxy() {
            return (FeedingRepository) Proxy.newProxyInstance(
                    FeedingRepository.class.getClassLoader(),
                    new Class<?>[]{FeedingRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("sumTotalFeedingCost".equals(methodName)) {
                            return totalFeedingCost;
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "AggregateFeedingRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }
    }

    private static class CountAnimalRepository {

        private Long count;

        AnimalRepository createProxy() {
            return (AnimalRepository) Proxy.newProxyInstance(
                    AnimalRepository.class.getClassLoader(),
                    new Class<?>[]{AnimalRepository.class},
                    (proxy, method, args) -> {
                        String methodName = method.getName();

                        if ("count".equals(methodName)) {
                            return count;
                        }
                        if ("equals".equals(methodName)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(methodName)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(methodName)) {
                            return "CountAnimalRepositoryProxy";
                        }

                        throw new UnsupportedOperationException("Method not supported in test: " + methodName);
                    });
        }
    }
}
