package com.jpsoftware.farmapp.production.dto;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Production data returned by the API.")
public class ProductionResponse {

    @Schema(description = "Production identifier.", example = "production-001")
    private String id;

    @Schema(description = "Animal identifier.", example = "animal-001")
    private String animalId;

    @Schema(description = "Production date.", example = "2026-03-20")
    private LocalDate date;

    @Schema(description = "Produced quantity in liters.", example = "32.8")
    private Double quantity;

    @Schema(description = "Embedded animal summary.")
    private AnimalSummaryResponse animal;

    public ProductionResponse() {
    }

    public ProductionResponse(String id, String animalId, LocalDate date, Double quantity) {
        this.id = id;
        this.animalId = animalId;
        this.date = date;
        this.quantity = quantity;
    }

    public ProductionResponse(String id, String animalId, LocalDate date, Double quantity, AnimalSummaryResponse animal) {
        this(id, animalId, date, quantity);
        this.animal = animal;
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

    public AnimalSummaryResponse getAnimal() {
        return animal;
    }

    public void setAnimal(AnimalSummaryResponse animal) {
        this.animal = animal;
    }
}
