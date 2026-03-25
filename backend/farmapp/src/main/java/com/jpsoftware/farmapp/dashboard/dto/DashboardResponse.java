package com.jpsoftware.farmapp.dashboard.dto;

public class DashboardResponse {

    private Double totalProduction;
    private Double totalFeedingCost;
    private Double totalRevenue;
    private Double totalProfit;
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
