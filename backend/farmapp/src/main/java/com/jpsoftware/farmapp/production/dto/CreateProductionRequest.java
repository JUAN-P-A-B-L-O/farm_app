package com.jpsoftware.farmapp.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class CreateProductionRequest {

    @NotBlank
    private String animalId;

    @NotNull
    private LocalDate date;

    @NotNull
    @Positive
    private Double quantity;

    @NotBlank
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
