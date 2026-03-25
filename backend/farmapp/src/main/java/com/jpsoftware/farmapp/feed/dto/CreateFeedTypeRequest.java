package com.jpsoftware.farmapp.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateFeedTypeRequest {

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotNull(message = "costPerKg must not be null")
    @Positive(message = "costPerKg must be greater than zero")
    private Double costPerKg;

    public CreateFeedTypeRequest() {
    }

    public CreateFeedTypeRequest(String name, Double costPerKg) {
        this.name = name;
        this.costPerKg = costPerKg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCostPerKg() {
        return costPerKg;
    }

    public void setCostPerKg(Double costPerKg) {
        this.costPerKg = costPerKg;
    }
}
