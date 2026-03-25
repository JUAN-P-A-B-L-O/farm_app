package com.jpsoftware.farmapp.feeding.dto;

import java.time.LocalDate;

public class FeedingResponse {

    private String id;
    private String animalId;
    private String feedTypeId;
    private LocalDate date;
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
