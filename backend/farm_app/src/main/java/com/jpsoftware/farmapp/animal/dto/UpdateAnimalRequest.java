package com.jpsoftware.farmapp.animal.dto;

import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAnimalRequest {

    private String tag;

    private String breed;

    private LocalDate birthDate;

    private String status;

    private String farmId;

    @AssertTrue(message = "Animal tag must not be blank")
    public boolean isTagValid() {
        return tag == null || StringUtils.hasText(tag);
    }

    @AssertTrue(message = "Animal breed must not be blank")
    public boolean isBreedValid() {
        return breed == null || StringUtils.hasText(breed);
    }
}
