package com.jpsoftware.farmapp.farm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Farm data returned by the API.")
public class FarmResponse {

    @Schema(description = "Farm identifier.", example = "farm-001")
    private String id;

    @Schema(description = "Farm name.", example = "North Dairy")
    private String name;

    public FarmResponse() {
    }

    public FarmResponse(String id, String name) {
        this.id = id;
        this.name = name;
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
}
