package com.jpsoftware.farmapp.animal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating an animal.")
public class CreateAnimalRequest {

    @NotBlank
    @Schema(description = "Animal management tag.", example = "COW-101")
    private String tag;

    @NotBlank
    @Schema(description = "Animal breed.", example = "Holstein")
    private String breed;

    @NotNull
    @Schema(description = "Animal birth date.", example = "2022-01-15")
    private LocalDate birthDate;

    @NotBlank
    @Schema(description = "Animal origin.", example = "PURCHASED", allowableValues = {"PURCHASED", "BORN"})
    private String origin;

    @Positive(message = "Animal acquisitionCost must be greater than zero")
    @Schema(description = "Acquisition cost for purchased animals.", example = "1250.50")
    private Double acquisitionCost;

    @NotBlank
    @Schema(description = "Farm identifier where the animal belongs.", example = "farm-001")
    private String farmId;
}
