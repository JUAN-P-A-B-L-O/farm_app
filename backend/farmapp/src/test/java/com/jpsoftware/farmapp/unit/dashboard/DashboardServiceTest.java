package com.jpsoftware.farmapp.unit.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import com.jpsoftware.farmapp.feed.repository.FeedTypeRepository;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import com.jpsoftware.farmapp.feeding.repository.FeedingRepository;
import com.jpsoftware.farmapp.farm.service.FarmAccessService;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    private final ProductionRepository productionRepository = org.mockito.Mockito.mock(ProductionRepository.class);
    private final FeedingRepository feedingRepository = org.mockito.Mockito.mock(FeedingRepository.class);
    private final AnimalRepository animalRepository = org.mockito.Mockito.mock(AnimalRepository.class);
    private final FeedTypeRepository feedTypeRepository = org.mockito.Mockito.mock(FeedTypeRepository.class);
    private final FarmAccessService farmAccessService = org.mockito.Mockito.mock(FarmAccessService.class);
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                productionRepository,
                feedingRepository,
                animalRepository,
                feedTypeRepository,
                farmAccessService);
    }

    @Test
    void shouldIncludeSaleRevenueInDashboardTotals() {
        when(productionRepository.findByFarmIdAndStatus("farm-1", ProductionEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new ProductionEntity("production-1", "animal-1", LocalDate.parse("2026-01-10"), 100.0, "manager-1", "farm-1", ProductionEntity.STATUS_ACTIVE)));
        when(feedingRepository.findByFarmIdAndStatus("farm-1", FeedingEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new FeedingEntity("feeding-1", "animal-1", "feed-type-1", LocalDate.parse("2026-01-11"), 10.0, "manager-1", "farm-1", FeedingEntity.STATUS_ACTIVE)));
        when(feedTypeRepository.findAllById(java.util.Set.of("feed-type-1"))).thenReturn(List.of(
                new FeedTypeEntity("feed-type-1", "Silage", 4.0, true, "farm-1")));
        when(animalRepository.findByFarmId("farm-1")).thenReturn(List.of(
                AnimalEntity.builder().id("animal-1").farmId("farm-1").status(AnimalEntity.STATUS_ACTIVE).acquisitionCost(50.0).salePrice(300.0).saleDate(LocalDate.parse("2026-01-15")).build(),
                AnimalEntity.builder().id("animal-2").farmId("farm-1").status(AnimalEntity.STATUS_ACTIVE).acquisitionCost(null).salePrice(null).build()));

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
        when(productionRepository.findByFarmIdAndStatus("farm-1", ProductionEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new ProductionEntity("production-1", "animal-1", LocalDate.parse("2026-01-10"), 100.0, "manager-1", "farm-1", ProductionEntity.STATUS_ACTIVE)));
        when(feedingRepository.findByFarmIdAndStatus("farm-1", FeedingEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new FeedingEntity("feeding-1", "animal-1", "feed-type-1", LocalDate.parse("2026-01-11"), 10.0, "manager-1", "farm-1", FeedingEntity.STATUS_ACTIVE)));
        when(feedTypeRepository.findAllById(java.util.Set.of("feed-type-1"))).thenReturn(List.of(
                new FeedTypeEntity("feed-type-1", "Silage", 4.0, true, "farm-1")));
        when(animalRepository.findByFarmId("farm-1")).thenReturn(List.of(
                AnimalEntity.builder().id("animal-1").farmId("farm-1").status(AnimalEntity.STATUS_ACTIVE).acquisitionCost(50.0).salePrice(300.0).saleDate(LocalDate.parse("2026-01-15")).build(),
                AnimalEntity.builder().id("animal-2").farmId("farm-1").status(AnimalEntity.STATUS_ACTIVE).acquisitionCost(null).salePrice(null).build()));

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
        when(productionRepository.findByFarmIdAndStatus("farm-1", ProductionEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new ProductionEntity("production-1", "animal-1", LocalDate.parse("2026-01-10"), 100.0, "manager-1", "farm-1", ProductionEntity.STATUS_ACTIVE)));
        when(feedingRepository.findByFarmIdAndStatus("farm-1", FeedingEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new FeedingEntity("feeding-1", "animal-1", "feed-type-1", LocalDate.parse("2026-01-11"), 10.0, "manager-1", "farm-1", FeedingEntity.STATUS_ACTIVE)));
        when(feedTypeRepository.findAllById(java.util.Set.of("feed-type-1"))).thenReturn(List.of(
                new FeedTypeEntity("feed-type-1", "Silage", 4.0, true, "farm-1")));
        when(animalRepository.findByFarmId("farm-1")).thenReturn(List.of(
                AnimalEntity.builder().id("animal-1").farmId("farm-1").status(AnimalEntity.STATUS_ACTIVE).acquisitionCost(50.0).salePrice(300.0).saleDate(LocalDate.parse("2026-01-15")).build(),
                AnimalEntity.builder().id("animal-2").farmId("farm-1").status(AnimalEntity.STATUS_ACTIVE).acquisitionCost(null).salePrice(null).build()));

        DashboardResponse response = dashboardService.getDashboard("farm-1", true, "USD");

        assertEquals(100.0, response.getTotalProduction());
        assertEquals(8.0, response.getTotalFeedingCost());
        assertEquals(100.0, response.getTotalRevenue());
        assertEquals(82.0, response.getTotalProfit());
        assertEquals(2L, response.getAnimalCount());
    }

    @Test
    void shouldFilterDashboardByDateAnimalAndStatus() {
        when(animalRepository.existsById("animal-1")).thenReturn(true);
        when(animalRepository.existsByIdAndFarmId("animal-1", "farm-1")).thenReturn(true);
        when(animalRepository.findByIdAndFarmId("animal-1", "farm-1")).thenReturn(java.util.Optional.of(
                AnimalEntity.builder()
                        .id("animal-1")
                        .farmId("farm-1")
                        .status(AnimalEntity.STATUS_SOLD)
                        .acquisitionCost(60.0)
                        .salePrice(200.0)
                        .saleDate(LocalDate.parse("2026-01-20"))
                        .build()));
        when(productionRepository.findByFarmIdAndStatus("farm-1", ProductionEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new ProductionEntity("production-1", "animal-1", LocalDate.parse("2026-01-12"), 50.0, "manager-1", "farm-1", ProductionEntity.STATUS_ACTIVE),
                new ProductionEntity("production-2", "animal-1", LocalDate.parse("2026-02-01"), 75.0, "manager-1", "farm-1", ProductionEntity.STATUS_ACTIVE)));
        when(feedingRepository.findByFarmIdAndStatus("farm-1", FeedingEntity.STATUS_ACTIVE)).thenReturn(List.of(
                new FeedingEntity("feeding-1", "animal-1", "feed-type-1", LocalDate.parse("2026-01-13"), 5.0, "manager-1", "farm-1", FeedingEntity.STATUS_ACTIVE),
                new FeedingEntity("feeding-2", "animal-1", "feed-type-1", LocalDate.parse("2026-02-02"), 7.0, "manager-1", "farm-1", FeedingEntity.STATUS_ACTIVE)));
        when(feedTypeRepository.findAllById(java.util.Set.of("feed-type-1"))).thenReturn(List.of(
                new FeedTypeEntity("feed-type-1", "Silage", 4.0, true, "farm-1")));

        DashboardResponse response = dashboardService.getDashboard(
                "farm-1",
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-01-31"),
                "animal-1",
                AnimalEntity.STATUS_SOLD,
                true);

        assertEquals(50.0, response.getTotalProduction());
        assertEquals(20.0, response.getTotalFeedingCost());
        assertEquals(300.0, response.getTotalRevenue());
        assertEquals(220.0, response.getTotalProfit());
        assertEquals(1L, response.getAnimalCount());
    }

    @Test
    void shouldRejectInvalidStatusFilter() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dashboardService.getDashboard("farm-1", null, null, null, "UNKNOWN", true));

        assertEquals("status must be ACTIVE, INACTIVE, SOLD, or DEAD", exception.getMessage());
    }
}
