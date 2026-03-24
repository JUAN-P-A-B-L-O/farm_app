package com.jpsoftware.farmapp.animal.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnimalRequest {

    @NotBlank
    private String tag;

    @NotBlank
    private String breed;

    private LocalDate birthDate;

    @NotBlank
    private String farmId;
}
