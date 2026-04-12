package com.jpsoftware.farmapp.feeding.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Request payload for updating a feeding record.")
public class UpdateFeedingRequest {

    @JsonAlias("animal")
    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @Schema(description = "Feed type identifier.", example = "feed-type-001")
    private String feedTypeId;

    @Schema(description = "Feeding date.", example = "2026-03-20")
    private LocalDate date;

    @Positive(message = "quantity must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "quantity must have at most 2 decimal places")
    @Schema(description = "Feed quantity in kilograms.", example = "14.5")
    private Double quantity;

    public UpdateFeedingRequest() {
    }

    public UpdateFeedingRequest(String animalId, String feedTypeId, LocalDate date, Double quantity) {
        this.animalId = animalId;
        this.feedTypeId = feedTypeId;
        this.date = date;
        this.quantity = quantity;
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
}
