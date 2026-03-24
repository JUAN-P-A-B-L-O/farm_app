package com.jpsoftware.farmapp.animal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private LocalDate birthDate;

    @NotBlank
    private String farmId;
}
