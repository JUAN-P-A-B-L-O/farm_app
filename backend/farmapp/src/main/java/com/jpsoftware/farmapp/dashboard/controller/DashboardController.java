package com.jpsoftware.farmapp.dashboard.controller;

import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import com.jpsoftware.farmapp.shared.util.CsvResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @RequestParam(required = false, defaultValue = "true") boolean includeAcquisitionCost,
            @RequestParam(required = false) String currency) {
        DashboardResponse response = StringUtils.hasText(currency)
                ? dashboardService.getDashboard(farmId, includeAcquisitionCost, currency)
                : dashboardService.getDashboard(farmId, includeAcquisitionCost);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/export")
    @Operation(summary = "Export dashboard", description = "Exports dashboard metrics as CSV using the current farm and acquisition-cost settings.")
    public ResponseEntity<byte[]> exportDashboard(
            @RequestParam(required = false) String farmId,
            @RequestParam(required = false, defaultValue = "true") boolean includeAcquisitionCost,
            @RequestParam(required = false) String currency) {
        return CsvResponseFactory.buildDownload(
                "dashboard-summary.csv",
                StringUtils.hasText(currency)
                        ? dashboardService.exportDashboard(farmId, includeAcquisitionCost, currency)
                        : dashboardService.exportDashboard(farmId, includeAcquisitionCost));
    }
}
