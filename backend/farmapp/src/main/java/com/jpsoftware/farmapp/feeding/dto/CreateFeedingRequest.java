package com.jpsoftware.farmapp.feeding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class CreateFeedingRequest {

    @NotBlank(message = "animalId must not be blank")
    private String animalId;

    @NotBlank(message = "feedTypeId must not be blank")
    private String feedTypeId;

    @NotNull(message = "date must not be null")
    private LocalDate date;

    @NotNull(message = "quantity must not be null")
    @Positive(message = "quantity must be greater than zero")
    private Double quantity;

    @NotBlank(message = "userId must not be blank")
    private String userId;

    public CreateFeedingRequest() {
    }

    public CreateFeedingRequest(String animalId, String feedTypeId, LocalDate date, Double quantity, String userId) {
        this.animalId = animalId;
        this.feedTypeId = feedTypeId;
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

    public String getFeedTypeId() {
        return feedTypeId;
    }

    public void setFeedTypeId(String feedTypeId) {
        this.feedTypeId = feedTypeId;
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
