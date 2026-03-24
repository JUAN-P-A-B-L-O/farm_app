package com.jpsoftware.farmapp.production.dto;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class UpdateProductionRequest {

    private LocalDate date;

    @Positive
    private Double quantity;

    public UpdateProductionRequest() {
    }

    public UpdateProductionRequest(LocalDate date, Double quantity) {
        this.date = date;
        this.quantity = quantity;
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
