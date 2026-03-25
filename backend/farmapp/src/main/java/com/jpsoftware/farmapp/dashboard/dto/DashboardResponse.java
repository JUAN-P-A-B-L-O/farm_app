package com.jpsoftware.farmapp.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated dashboard metrics.")
public class DashboardResponse {

    @Schema(description = "Total milk production across the farm.", example = "1250.5")
    private Double totalProduction;

    @Schema(description = "Total feeding cost across the farm.", example = "780.25")
    private Double totalFeedingCost;

    @Schema(description = "Total revenue based on production.", example = "2501.0")
    private Double totalRevenue;

    @Schema(description = "Total profit after feeding costs.", example = "1720.75")
    private Double totalProfit;

    @Schema(description = "Total number of registered animals.", example = "48")
    private Long animalCount;

    public DashboardResponse() {
    }

    public DashboardResponse(
            Double totalProduction,
            Double totalFeedingCost,
            Double totalRevenue,
            Double totalProfit,
            Long animalCount) {
        this.totalProduction = totalProduction;
        this.totalFeedingCost = totalFeedingCost;
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
        this.animalCount = animalCount;
    }

    public Double getTotalProduction() {
        return totalProduction;
    }

    public void setTotalProduction(Double totalProduction) {
        this.totalProduction = totalProduction;
    }

    public Double getTotalFeedingCost() {
        return totalFeedingCost;
    }

    public void setTotalFeedingCost(Double totalFeedingCost) {
        this.totalFeedingCost = totalFeedingCost;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Long getAnimalCount() {
        return animalCount;
    }

    public void setAnimalCount(Long animalCount) {
        this.animalCount = animalCount;
    }
}
