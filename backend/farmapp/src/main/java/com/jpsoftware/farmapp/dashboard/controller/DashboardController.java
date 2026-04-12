package com.jpsoftware.farmapp.dashboard.controller;

import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
import com.jpsoftware.farmapp.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<DashboardResponse> getDashboard(@RequestParam(required = false) String farmId) {
        DashboardResponse response = dashboardService.getDashboard(farmId);
        return ResponseEntity.ok(response);
    }
}
