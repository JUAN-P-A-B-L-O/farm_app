package com.jpsoftware.farmapp.animalbatch.dto;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Animal batch response.")
public class AnimalBatchResponse {

    private String id;
    private String name;
    private String farmId;
    private List<AnimalSummaryResponse> animals;

    public AnimalBatchResponse() {
    }

    public AnimalBatchResponse(String id, String name, String farmId, List<AnimalSummaryResponse> animals) {
        this.id = id;
        this.name = name;
        this.farmId = farmId;
        this.animals = animals;
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

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public List<AnimalSummaryResponse> getAnimals() {
        return animals;
    }

    public void setAnimals(List<AnimalSummaryResponse> animals) {
        this.animals = animals;
    }
}
