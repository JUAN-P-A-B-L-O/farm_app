package com.jpsoftware.farmapp.feeding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Feeding data returned by the API.")
public class FeedingResponse {

    @Schema(description = "Feeding identifier.", example = "feeding-001")
    private String id;

    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @Schema(description = "Feed type identifier.", example = "feed-type-001")
    private String feedTypeId;

    @Schema(description = "Feeding date.", example = "2026-03-20")
    private LocalDate date;

    @Schema(description = "Feed quantity in kilograms.", example = "14.5")
    private Double quantity;

    public FeedingResponse() {
    }

    public FeedingResponse(String id, String animalId, String feedTypeId, LocalDate date, Double quantity) {
        this.id = id;
        this.animalId = animalId;
        this.feedTypeId = feedTypeId;
        this.date = date;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
