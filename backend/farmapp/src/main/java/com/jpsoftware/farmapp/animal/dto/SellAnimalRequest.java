package com.jpsoftware.farmapp.animal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for selling an animal.")
public class SellAnimalRequest {

    @NotNull(message = "Animal salePrice must not be null")
    @Positive(message = "Animal salePrice must be greater than zero")
    @Schema(description = "Sale price received for the animal.", example = "3200.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double salePrice;

    @Schema(description = "Sale date. Defaults to the current date when omitted.", example = "2026-04-14")
    private LocalDate saleDate;
}
