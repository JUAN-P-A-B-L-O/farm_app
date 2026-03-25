package com.jpsoftware.farmapp.feed.dto;

public class FeedTypeResponse {

    private String id;
    private String name;
    private Double costPerKg;
    private Boolean active;

    public FeedTypeResponse() {
    }

    public FeedTypeResponse(String id, String name, Double costPerKg, Boolean active) {
        this.id = id;
        this.name = name;
        this.costPerKg = costPerKg;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
