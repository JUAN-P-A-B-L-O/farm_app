package com.jpsoftware.farmapp.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Production summary grouped by animal.")
public class ProductionSummaryResponse {

    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @Schema(description = "Total produced quantity.", example = "320.0")
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
