package com.jpsoftware.farmapp.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profit summary for production by animal.")
public class ProductionProfitResponse {

    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @Schema(description = "Total produced quantity.", example = "320.0")
    private Double totalProduction;

    @Schema(description = "Total feeding cost for the animal.", example = "185.5")
    private Double totalFeedingCost;

    @Schema(description = "Milk price used in the profit calculation.", example = "2.0")
    private Double milkPrice;

    @Schema(description = "Calculated revenue.", example = "640.0")
    private Double revenue;

    @Schema(description = "Calculated profit.", example = "454.5")
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
