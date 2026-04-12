package com.jpsoftware.farmapp.production.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Request payload for updating a production record.")
public class UpdateProductionRequest {

    @JsonAlias("animal")
    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @Schema(description = "Production date.", example = "2026-03-21")
    private LocalDate date;

    @Positive
    @Schema(description = "Produced quantity in liters.", example = "34.2")
    private Double quantity;

    public UpdateProductionRequest() {
    }

    public UpdateProductionRequest(LocalDate date, Double quantity) {
        this(null, date, quantity);
    }

    public UpdateProductionRequest(String animalId, LocalDate date, Double quantity) {
        this.animalId = animalId;
        this.date = date;
        this.quantity = quantity;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
