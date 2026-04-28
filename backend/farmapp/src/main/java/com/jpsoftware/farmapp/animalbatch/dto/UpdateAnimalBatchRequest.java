package com.jpsoftware.farmapp.animalbatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request payload for updating an animal batch.")
public class UpdateAnimalBatchRequest {

    @NotBlank(message = "name must not be blank")
    @Schema(description = "Batch name.", example = "Lote das matrizes")
    private String name;

    @NotEmpty(message = "animalIds must not be empty")
    @Schema(description = "Animal identifiers assigned to the batch.")
    private List<@NotBlank(message = "animalId must not be blank") String> animalIds;

    public UpdateAnimalBatchRequest() {
    }

    public UpdateAnimalBatchRequest(String name, List<String> animalIds) {
        this.name = name;
        this.animalIds = animalIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAnimalIds() {
        return animalIds;
    }

    public void setAnimalIds(List<String> animalIds) {
        this.animalIds = animalIds;
    }
}
