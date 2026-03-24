package com.jpsoftware.farmapp.animal.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AnimalResponse {

    private final String id;
    private final String tag;
    private final String breed;
    private final LocalDate birthDate;
    private final String status;
    private final String farmId;
}
