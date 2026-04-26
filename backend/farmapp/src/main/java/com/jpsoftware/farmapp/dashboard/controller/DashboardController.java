package com.jpsoftware.farmapp.dashboard.controller;

import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.shared.util.CsvResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Dashboard", description = "Operations for retrieving aggregated dashboard metrics.")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard", description = "Returns aggregated production, feeding cost, revenue, profit, and animal count metrics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    })
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) List<String> animalIds,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "true") boolean includeAcquisitionCost,
            @RequestParam(required = false) String currency) {
        boolean hasMultiAnimalFilter = hasAnimalIds(animalIds);
        DashboardResponse response = StringUtils.hasText(currency)
                ? hasMultiAnimalFilter
                        ? dashboardService.getDashboardByAnimals(
                                farmId,
                                startDate,
                                endDate,
                                mergeAnimalFilters(animalId, animalIds),
                                status,
                                includeAcquisitionCost,
                                currency)
                        : dashboardService.getDashboard(farmId, startDate, endDate, animalId, status, includeAcquisitionCost, currency)
                : hasMultiAnimalFilter
                        ? dashboardService.getDashboardByAnimals(
                                farmId,
                                startDate,
                                endDate,
                                mergeAnimalFilters(animalId, animalIds),
                                status,
                                includeAcquisitionCost)
                        : dashboardService.getDashboard(farmId, startDate, endDate, animalId, status, includeAcquisitionCost);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/export")
    @Operation(summary = "Export dashboard", description = "Exports dashboard metrics as CSV using the current farm and acquisition-cost settings.")
    public ResponseEntity<byte[]> exportDashboard(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) List<String> animalIds,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "true") boolean includeAcquisitionCost,
            @RequestParam(required = false) String currency) {
        boolean hasMultiAnimalFilter = hasAnimalIds(animalIds);
        return CsvResponseFactory.buildDownload(
                "dashboard-summary.csv",
                StringUtils.hasText(currency)
                        ? hasMultiAnimalFilter
                                ? dashboardService.exportDashboardByAnimals(
                                        farmId,
                                        startDate,
                                        endDate,
                                        mergeAnimalFilters(animalId, animalIds),
                                        status,
                                        includeAcquisitionCost,
                                        currency)
                                : dashboardService.exportDashboard(farmId, startDate, endDate, animalId, status, includeAcquisitionCost, currency)
                        : hasMultiAnimalFilter
                                ? dashboardService.exportDashboardByAnimals(
                                        farmId,
                                        startDate,
                                        endDate,
                                        mergeAnimalFilters(animalId, animalIds),
                                        status,
                                        includeAcquisitionCost)
                                : dashboardService.exportDashboard(farmId, startDate, endDate, animalId, status, includeAcquisitionCost));
    }

    private boolean hasAnimalIds(List<String> animalIds) {
        return animalIds != null && animalIds.stream().anyMatch(StringUtils::hasText);
    }

    private List<String> mergeAnimalFilters(String animalId, List<String> animalIds) {
        LinkedHashSet<String> mergedAnimalIds = new LinkedHashSet<>();
        if (StringUtils.hasText(animalId)) {
            mergedAnimalIds.add(animalId.trim());
        }
        if (animalIds != null) {
            animalIds.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(mergedAnimalIds::add);
        }
        return List.copyOf(mergedAnimalIds);
    }
}
