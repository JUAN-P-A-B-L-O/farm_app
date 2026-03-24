package com.jpsoftware.farmapp.production.dto;

import java.time.LocalDate;

public class ProductionResponse {

    private String id;
    private String animalId;
    private LocalDate date;
    private Double quantity;

    public ProductionResponse() {
    }

    public ProductionResponse(String id, String animalId, LocalDate date, Double quantity) {
        this.id = id;
        this.animalId = animalId;
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
