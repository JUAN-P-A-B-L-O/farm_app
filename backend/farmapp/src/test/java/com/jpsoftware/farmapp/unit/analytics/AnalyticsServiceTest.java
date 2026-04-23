package com.jpsoftware.farmapp.unit.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.analytics.dto.AnalyticsProfitPointResponse;
import com.jpsoftware.farmapp.analytics.service.AnalyticsService;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnalyticsServiceTest {

    private final ProductionRepository productionRepository = org.mockito.Mockito.mock(ProductionRepository.class);
    private final FeedingRepository feedingRepository = org.mockito.Mockito.mock(FeedingRepository.class);
    private final AnimalRepository animalRepository = org.mockito.Mockito.mock(AnimalRepository.class);
    private final FeedTypeRepository feedTypeRepository = org.mockito.Mockito.mock(FeedTypeRepository.class);
    private final FarmAccessService farmAccessService = org.mockito.Mockito.mock(FarmAccessService.class);
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                productionRepository,
                feedingRepository,
                animalRepository,
                feedTypeRepository,
                farmAccessService);
    }

    @Test
    void shouldIncludeSaleRevenueInProfitSeries() {
        ProductionEntity production = new ProductionEntity(
                "prod-1",
                "animal-1",
                LocalDate.of(2026, 4, 14),
                10.0,
                "user-1",
                "farm-1",
                ProductionEntity.STATUS_ACTIVE);
        AnimalEntity animal = AnimalEntity.builder()
                .id("animal-1")
                .farmId("farm-1")
                .acquisitionCost(50.0)
                .salePrice(300.0)
                .saleDate(LocalDate.of(2026, 4, 14))
                .build();

        when(productionRepository.findByFarmIdAndStatus("farm-1", ProductionEntity.STATUS_ACTIVE)).thenReturn(List.of(production));
        when(feedingRepository.findByFarmIdAndStatus("farm-1", "ACTIVE")).thenReturn(List.of());
        when(animalRepository.findByIdAndFarmId("animal-1", "farm-1")).thenReturn(Optional.of(animal));
        when(animalRepository.existsById("animal-1")).thenReturn(true);
        when(animalRepository.existsByIdAndFarmId("animal-1", "farm-1")).thenReturn(true);

        List<AnalyticsProfitPointResponse> response = analyticsService.getProfitSeries(
                null,
                null,
                "animal-1",
                "day",
                "farm-1",
                true);

        assertEquals(1, response.size());
        assertEquals("2026-04-14", response.getFirst().getPeriod());
        assertEquals(10.0, response.getFirst().getProduction());
        assertEquals(50.0, response.getFirst().getFeedingCost());
        assertEquals(320.0, response.getFirst().getRevenue());
        assertEquals(270.0, response.getFirst().getProfit());
    }

    @Test
    void shouldExportProfitSeriesAsCsv() {
        ProductionEntity production = new ProductionEntity(
                "prod-1",
                "animal-1",
                LocalDate.of(2026, 4, 14),
                10.0,
                "user-1",
                "farm-1",
                ProductionEntity.STATUS_ACTIVE);
        AnimalEntity animal = AnimalEntity.builder()
                .id("animal-1")
                .farmId("farm-1")
                .acquisitionCost(50.0)
                .salePrice(300.0)
                .saleDate(LocalDate.of(2026, 4, 14))
                .build();

        when(productionRepository.findByFarmIdAndStatus("farm-1", ProductionEntity.STATUS_ACTIVE)).thenReturn(List.of(production));
        when(feedingRepository.findByFarmIdAndStatus("farm-1", "ACTIVE")).thenReturn(List.of());
        when(animalRepository.findByIdAndFarmId("animal-1", "farm-1")).thenReturn(Optional.of(animal));
        when(animalRepository.existsById("animal-1")).thenReturn(true);
        when(animalRepository.existsByIdAndFarmId("animal-1", "farm-1")).thenReturn(true);

        String csv = analyticsService.exportProfitSeries(null, null, "animal-1", "day", "farm-1", true);

        assertEquals(
                """
                        period,production,feedingCost,revenue,profit
                        2026-04-14,10.0,50.0,320.0,270.0
                        """,
                csv);
    }
}
