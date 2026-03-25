package com.jpsoftware.farmapp.animal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload for partially updating an animal.")
public class UpdateAnimalRequest {

    @Schema(description = "Animal management tag.", example = "COW-101")
    private String tag;

    @Schema(description = "Animal breed.", example = "Jersey")
    private String breed;

    @Schema(description = "Animal birth date.", example = "2022-01-15")
    private LocalDate birthDate;

    @Schema(description = "Current animal status.", example = "ACTIVE")
    private String status;

    @Schema(description = "Farm identifier where the animal belongs.", example = "farm-001")
    private String farmId;

    @AssertTrue(message = "Animal tag must not be blank")
    @Schema(hidden = true)
    public boolean isTagValid() {
        return tag == null || StringUtils.hasText(tag);
    }

    @AssertTrue(message = "Animal breed must not be blank")
    @Schema(hidden = true)
    public boolean isBreedValid() {
        return breed == null || StringUtils.hasText(breed);
    }

    @AssertTrue(message = "Animal status must not be blank")
    @Schema(hidden = true)
    public boolean isStatusValid() {
        return status == null || StringUtils.hasText(status);
    }

    @AssertTrue(message = "Animal farmId must not be blank")
    @Schema(hidden = true)
    public boolean isFarmIdValid() {
        return farmId == null || StringUtils.hasText(farmId);
    }
}
