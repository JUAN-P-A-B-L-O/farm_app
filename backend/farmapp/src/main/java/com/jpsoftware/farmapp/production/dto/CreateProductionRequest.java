package com.jpsoftware.farmapp.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Request payload for creating a production record.")
public class CreateProductionRequest {

    @NotBlank
    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @NotNull
    @Schema(description = "Production date.", example = "2026-03-20")
    private LocalDate date;

    @NotNull
    @Positive
    @Schema(description = "Produced quantity in liters.", example = "32.8")
    private Double quantity;

    @NotBlank
    @Schema(description = "User identifier responsible for the record.", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    public CreateProductionRequest() {
    }

    public CreateProductionRequest(String animalId, LocalDate date, Double quantity, String userId) {
        this.animalId = animalId;
        this.date = date;
        this.quantity = quantity;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
