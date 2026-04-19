package com.jpsoftware.farmapp.feeding.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Request payload for creating a feeding record.")
public class CreateFeedingRequest {

    @JsonAlias("animal")
    @NotBlank(message = "animalId must not be blank")
    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @NotBlank(message = "feedTypeId must not be blank")
    @Schema(description = "Feed type identifier.", example = "feed-type-001")
    private String feedTypeId;

    @Schema(description = "Feeding date.", example = "2026-03-20")
    private LocalDate date;

    @NotNull(message = "quantity must not be null")
    @Positive(message = "quantity must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "quantity must have at most 2 decimal places")
    @Schema(description = "Feed quantity in kilograms.", example = "14.5")
    private Double quantity;

    @NotBlank(message = "userId must not be blank")
    @Schema(description = "User identifier responsible for the record.", example = "550e8400-e29b-41d4-a716-446655440000")
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
