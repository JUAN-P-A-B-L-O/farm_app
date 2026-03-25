package com.jpsoftware.farmapp.fixture;

import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import java.time.LocalDate;

public final class ProductionFixture {

    public static final String DEFAULT_USER_ID = "11111111-1111-1111-1111-111111111111";

    private ProductionFixture() {
    }

    public static ProductionEntity productionEntity() {
        return productionEntity("production-1", "animal-1", LocalDate.of(2026, 3, 20), 12.5, DEFAULT_USER_ID);
    }

    public static ProductionEntity productionEntity(
            String id,
            String animalId,
            LocalDate date,
            Double quantity,
            String createdBy) {
        ProductionEntity entity = new ProductionEntity();
        entity.setId(id);
        entity.setAnimalId(animalId);
        entity.setDate(date);
        entity.setQuantity(quantity);
        entity.setCreatedBy(createdBy);
        return entity;
    }

    public static String createRequestJson(String animalId, String userId) {
        return """
                {
                  "animalId": "%s",
                  "date": "2026-03-20",
                  "quantity": 12.5,
                  "userId": "%s"
                }
                """.formatted(animalId, userId);
    }

    public static String updateRequestJson() {
        return """
                {
                  "date": "2026-03-21",
                  "quantity": 15.0
                }
                """;
    }
}
