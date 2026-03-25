package com.jpsoftware.farmapp.feed.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Feed type data returned by the API.")
public class FeedTypeResponse {

    @Schema(description = "Feed type identifier.", example = "feed-type-001")
    private String id;

    @Schema(description = "Feed type name.", example = "Corn Silage")
    private String name;

    @Schema(description = "Feed cost per kilogram.", example = "1.85")
    private Double costPerKg;

    @Schema(description = "Indicates whether the feed type is active.", example = "true")
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
