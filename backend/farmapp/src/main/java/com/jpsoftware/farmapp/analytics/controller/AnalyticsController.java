package com.jpsoftware.farmapp.analytics.controller;

import com.jpsoftware.farmapp.analytics.dto.AnalyticsAnimalProductionPointResponse;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsProfitPointResponse;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsTimeSeriesPointResponse;
import com.jpsoftware.farmapp.analytics.service.AnalyticsService;
import com.jpsoftware.farmapp.shared.util.CsvResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Aggregated analytics for charts and dashboards.")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/production")
    @Operation(summary = "Get production analytics", description = "Returns aggregated production over time.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production analytics retrieved successfully")
    })
    public ResponseEntity<List<AnalyticsTimeSeriesPointResponse>> getProductionAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(analyticsService.getProductionSeries(startDate, endDate, animalId, groupBy, farmId));
    }

    @GetMapping("/production/export")
    @Operation(summary = "Export production analytics", description = "Exports production analytics as CSV using the current filters.")
    public ResponseEntity<byte[]> exportProductionAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @RequestParam(required = false) String productionUnit) {
        return CsvResponseFactory.buildDownload(
                "analytics-production.csv",
                StringUtils.hasText(productionUnit)
                        ? analyticsService.exportProductionSeries(startDate, endDate, animalId, groupBy, farmId, productionUnit)
                        : analyticsService.exportProductionSeries(startDate, endDate, animalId, groupBy, farmId));
    }

    @GetMapping("/feeding")
    @Operation(summary = "Get feeding analytics", description = "Returns aggregated feeding cost over time.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feeding analytics retrieved successfully")
    })
    public ResponseEntity<List<AnalyticsTimeSeriesPointResponse>> getFeedingAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(StringUtils.hasText(currency)
                ? analyticsService.getFeedingCostSeries(startDate, endDate, animalId, groupBy, farmId, currency)
                : analyticsService.getFeedingCostSeries(startDate, endDate, animalId, groupBy, farmId));
    }

    @GetMapping("/feeding/export")
    @Operation(summary = "Export feeding analytics", description = "Exports feeding analytics as CSV using the current filters.")
    public ResponseEntity<byte[]> exportFeedingAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return CsvResponseFactory.buildDownload(
                "analytics-feeding.csv",
                StringUtils.hasText(currency)
                        ? analyticsService.exportFeedingCostSeries(startDate, endDate, animalId, groupBy, farmId, currency)
                        : analyticsService.exportFeedingCostSeries(startDate, endDate, animalId, groupBy, farmId));
    }

    @GetMapping("/profit")
    @Operation(summary = "Get profit analytics", description = "Returns aggregated profit evolution over time.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profit analytics retrieved successfully")
    })
    public ResponseEntity<List<AnalyticsProfitPointResponse>> getProfitAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "true") boolean includeAcquisitionCost,
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(StringUtils.hasText(currency)
                ? analyticsService.getProfitSeries(
                        startDate,
                        endDate,
                        animalId,
                        groupBy,
                        farmId,
                        includeAcquisitionCost,
                        currency)
                : analyticsService.getProfitSeries(
                        startDate,
                        endDate,
                        animalId,
                        groupBy,
                        farmId,
                        includeAcquisitionCost));
    }

    @GetMapping("/profit/export")
    @Operation(summary = "Export profit analytics", description = "Exports profit analytics as CSV using the current filters.")
    public ResponseEntity<byte[]> exportProfitAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, defaultValue = "true") boolean includeAcquisitionCost,
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @RequestParam(required = false) String productionUnit) {
        return CsvResponseFactory.buildDownload(
                "analytics-profit.csv",
                StringUtils.hasText(productionUnit)
                        ? analyticsService.exportProfitSeries(
                                startDate,
                                endDate,
                                animalId,
                                groupBy,
                                farmId,
                                includeAcquisitionCost,
                                currency,
                                productionUnit)
                        : StringUtils.hasText(currency)
                        ? analyticsService.exportProfitSeries(
                                startDate,
                                endDate,
                                animalId,
                                groupBy,
                                farmId,
                                includeAcquisitionCost,
                                currency)
                        : analyticsService.exportProfitSeries(
                                startDate,
                                endDate,
                                animalId,
                                groupBy,
                                farmId,
                                includeAcquisitionCost));
    }

    @GetMapping("/production/by-animal")
    @Operation(summary = "Get production by animal", description = "Returns aggregated production grouped by animal.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Production by animal retrieved successfully")
    })
    public ResponseEntity<List<AnalyticsAnimalProductionPointResponse>> getProductionByAnimal(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency) {
        return ResponseEntity.ok(analyticsService.getProductionByAnimal(startDate, endDate, animalId, farmId));
    }

    @GetMapping("/production/by-animal/export")
    @Operation(summary = "Export production by animal", description = "Exports production grouped by animal as CSV using the current filters.")
    public ResponseEntity<byte[]> exportProductionByAnimal(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String productionUnit) {
        return CsvResponseFactory.buildDownload(
                "analytics-production-by-animal.csv",
                StringUtils.hasText(productionUnit)
                        ? analyticsService.exportProductionByAnimal(startDate, endDate, animalId, farmId, productionUnit)
                        : analyticsService.exportProductionByAnimal(startDate, endDate, animalId, farmId));
    }
}
