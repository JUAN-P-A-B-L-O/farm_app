package com.jpsoftware.farmapp.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AnalyticsAnimalProductionPointResponse {

    @Schema(description = "Animal identifier.", example = "animal-123")
    private final String animalId;

    @Schema(description = "Animal tag.", example = "COW-101")
    private final String animalTag;

    @Schema(description = "Aggregated production quantity.", example = "350.0")
    private final Double quantity;

    public AnalyticsAnimalProductionPointResponse(String animalId, String animalTag, Double quantity) {
        this.animalId = animalId;
        this.animalTag = animalTag;
        this.quantity = quantity;
    }

    public String getAnimalId() {
        return animalId;
    }

    public String getAnimalTag() {
        return animalTag;
    }

    public Double getQuantity() {
        return quantity;
    }
}
