package com.jpsoftware.farmapp.production.dto;

public class ProductionProfitResponse {

    private String animalId;
    private Double totalProduction;
    private Double totalFeedingCost;
    private Double milkPrice;
    private Double revenue;
    private Double profit;

    public ProductionProfitResponse() {
    }

    public ProductionProfitResponse(
            String animalId,
            Double totalProduction,
            Double totalFeedingCost,
            Double milkPrice,
            Double revenue,
            Double profit) {
        this.animalId = animalId;
        this.totalProduction = totalProduction;
        this.totalFeedingCost = totalFeedingCost;
        this.milkPrice = milkPrice;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
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

    public Double getMilkPrice() {
        return milkPrice;
    }

    public void setMilkPrice(Double milkPrice) {
        this.milkPrice = milkPrice;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }
}
