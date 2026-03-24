package com.jpsoftware.farmapp.production.dto;

public class ProductionSummaryResponse {

    private String animalId;
    private Double totalQuantity;

    public ProductionSummaryResponse() {
    }

    public ProductionSummaryResponse(String animalId, Double totalQuantity) {
        this.animalId = animalId;
        this.totalQuantity = totalQuantity;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public Double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
