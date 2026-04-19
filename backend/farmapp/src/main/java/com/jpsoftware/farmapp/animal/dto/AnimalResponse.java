package com.jpsoftware.farmapp.animal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Animal data returned by the API.")
public class AnimalResponse {

    @Schema(description = "Animal identifier.", example = "animal-001")
    private final String id;

    @Schema(description = "Animal management tag.", example = "COW-101")
    private final String tag;

    @Schema(description = "Animal breed.", example = "Holstein")
    private final String breed;

    @Schema(description = "Animal birth date.", example = "2022-01-15")
    private final LocalDate birthDate;

    @Schema(description = "Current animal status.", example = "ACTIVE")
    private final String status;

    @Schema(description = "Animal origin.", example = "PURCHASED")
    private final String origin;

    @Schema(description = "Acquisition cost when the animal was purchased.", example = "1250.50")
    private final Double acquisitionCost;

    @Schema(description = "Sale price when the animal has been sold.", example = "3200.00")
    private final Double salePrice;

    @Schema(description = "Sale date when the animal has been sold.", example = "2026-04-14")
    private final LocalDate saleDate;

    @Schema(description = "Farm identifier where the animal belongs.", example = "farm-001")
    private final String farmId;
}
