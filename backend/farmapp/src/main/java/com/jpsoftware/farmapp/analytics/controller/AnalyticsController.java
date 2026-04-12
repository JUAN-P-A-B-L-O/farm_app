package com.jpsoftware.farmapp.analytics.controller;

import com.jpsoftware.farmapp.analytics.dto.AnalyticsAnimalProductionPointResponse;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsProfitPointResponse;
import com.jpsoftware.farmapp.analytics.dto.AnalyticsTimeSeriesPointResponse;
import com.jpsoftware.farmapp.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(analyticsService.getProductionSeries(startDate, endDate, animalId, groupBy, farmId));
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
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(analyticsService.getFeedingCostSeries(startDate, endDate, animalId, groupBy, farmId));
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
            @RequestParam(required = false, defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(analyticsService.getProfitSeries(startDate, endDate, animalId, groupBy, farmId));
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
            @RequestParam(required = false) String farmId) {
        return ResponseEntity.ok(analyticsService.getProductionByAnimal(startDate, endDate, animalId, farmId));
    }
}
