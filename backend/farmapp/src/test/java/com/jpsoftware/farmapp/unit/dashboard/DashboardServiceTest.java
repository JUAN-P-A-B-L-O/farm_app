package com.jpsoftware.farmapp.unit.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    private final ProductionRepository productionRepository = org.mockito.Mockito.mock(ProductionRepository.class);
    private final FeedingRepository feedingRepository = org.mockito.Mockito.mock(FeedingRepository.class);
    private final AnimalRepository animalRepository = org.mockito.Mockito.mock(AnimalRepository.class);
    private final FarmAccessService farmAccessService = org.mockito.Mockito.mock(FarmAccessService.class);
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                productionRepository,
                feedingRepository,
                animalRepository,
                farmAccessService);
    }

    @Test
    void shouldIncludeSaleRevenueInDashboardTotals() {
        when(productionRepository.sumTotalProductionByFarmId("farm-1")).thenReturn(100.0);
        when(feedingRepository.sumTotalFeedingCostByFarmId("farm-1")).thenReturn(40.0);
        when(animalRepository.findByFarmId("farm-1")).thenReturn(List.of(
                AnimalEntity.builder().id("animal-1").farmId("farm-1").acquisitionCost(50.0).salePrice(300.0).build(),
                AnimalEntity.builder().id("animal-2").farmId("farm-1").acquisitionCost(null).salePrice(null).build()));
        when(animalRepository.countByFarmId("farm-1")).thenReturn(2L);

        DashboardResponse response = dashboardService.getDashboard("farm-1", true);

        assertEquals(100.0, response.getTotalProduction());
        assertEquals(40.0, response.getTotalFeedingCost());
        assertEquals(500.0, response.getTotalRevenue());
        assertEquals(410.0, response.getTotalProfit());
        assertEquals(2L, response.getAnimalCount());
        verify(farmAccessService).validateAccessibleFarmIfPresent("farm-1");
    }

    @Test
    void shouldExportDashboardAsCsv() {
        when(productionRepository.sumTotalProductionByFarmId("farm-1")).thenReturn(100.0);
        when(feedingRepository.sumTotalFeedingCostByFarmId("farm-1")).thenReturn(40.0);
        when(animalRepository.findByFarmId("farm-1")).thenReturn(List.of(
                AnimalEntity.builder().id("animal-1").farmId("farm-1").acquisitionCost(50.0).salePrice(300.0).build(),
                AnimalEntity.builder().id("animal-2").farmId("farm-1").acquisitionCost(null).salePrice(null).build()));
        when(animalRepository.countByFarmId("farm-1")).thenReturn(2L);

        String csv = dashboardService.exportDashboard("farm-1", true);

        assertEquals(
                """
totalProduction,totalFeedingCost,totalRevenue,totalProfit,animalCount
100.0,40.0,500.0,410.0,2
""",
                csv);
    }

    @Test
    void shouldConvertDashboardMonetaryTotalsWhenCurrencyIsProvided() {
        when(productionRepository.sumTotalProductionByFarmId("farm-1")).thenReturn(100.0);
        when(feedingRepository.sumTotalFeedingCostByFarmId("farm-1")).thenReturn(40.0);
        when(animalRepository.findByFarmId("farm-1")).thenReturn(List.of(
                AnimalEntity.builder().id("animal-1").farmId("farm-1").acquisitionCost(50.0).salePrice(300.0).build(),
                AnimalEntity.builder().id("animal-2").farmId("farm-1").acquisitionCost(null).salePrice(null).build()));
        when(animalRepository.countByFarmId("farm-1")).thenReturn(2L);

        DashboardResponse response = dashboardService.getDashboard("farm-1", true, "USD");

        assertEquals(100.0, response.getTotalProduction());
        assertEquals(8.0, response.getTotalFeedingCost());
        assertEquals(100.0, response.getTotalRevenue());
        assertEquals(82.0, response.getTotalProfit());
        assertEquals(2L, response.getAnimalCount());
    }
}
