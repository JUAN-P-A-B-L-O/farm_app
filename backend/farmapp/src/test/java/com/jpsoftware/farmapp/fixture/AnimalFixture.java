package com.jpsoftware.farmapp.fixture;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import java.time.LocalDate;

public final class AnimalFixture {

    private AnimalFixture() {
    }

    public static AnimalEntity animalEntity() {
        return animalEntity("animal-1", "TAG-001", "Angus", LocalDate.of(2022, 1, 10), "ACTIVE", "FARM-001");
    }

    public static AnimalEntity animalEntity(
            String id,
            String tag,
            String breed,
            LocalDate birthDate,
            String status,
            String farmId) {
        return AnimalEntity.builder()
                .id(id)
                .tag(tag)
                .breed(breed)
                .birthDate(birthDate)
                .status(status)
                .farmId(farmId)
                .build();
    }

    public static String createRequestJson() {
        return """
                {
                  "tag": "TAG-001",
                  "breed": "Angus",
                  "birthDate": "2022-01-10",
                  "farmId": "FARM-001"
                }
                """;
    }

    public static String updateRequestJson() {
        return """
                {
                  "tag": "TAG-002",
                  "breed": "Nelore",
                  "birthDate": "2021-05-20",
                  "status": "INACTIVE",
                  "farmId": "FARM-002"
                }
                """;
    }
}
