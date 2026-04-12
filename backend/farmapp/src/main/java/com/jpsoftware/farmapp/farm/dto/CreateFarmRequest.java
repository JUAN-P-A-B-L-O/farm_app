package com.jpsoftware.farmapp.farm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating a farm.")
public class CreateFarmRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "Farm name.", example = "North Dairy")
    private String name;

    public CreateFarmRequest() {
    }

    public CreateFarmRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
