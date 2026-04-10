package com.jpsoftware.farmapp.fixture;

import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import java.time.LocalDate;

public final class FeedingFixture {

    public static final String DEFAULT_USER_ID = "11111111-1111-1111-1111-111111111111";

    private FeedingFixture() {
    }

    public static FeedingEntity feedingEntity() {
        return feedingEntity("feeding-1", "animal-1", "feed-type-1", LocalDate.of(2026, 3, 24), 8.5, DEFAULT_USER_ID);
    }

    public static FeedingEntity feedingEntity(
            String id,
            String animalId,
            String feedTypeId,
            LocalDate date,
            Double quantity,
            String createdBy) {
        return new FeedingEntity(id, animalId, feedTypeId, date, quantity, createdBy);
    }

    public static String createRequestJson(String animalId, String feedTypeId, String userId) {
        return """
                {
                  "animalId": "%s",
                  "feedTypeId": "%s",
                  "date": "2026-03-24",
                  "quantity": 8.5,
                  "userId": "%s"
                }
                """.formatted(animalId, feedTypeId, userId);
    }

    public static String updateRequestJson(String animalId, String feedTypeId) {
        return """
                {
                  "animalId": "%s",
                  "feedTypeId": "%s",
                  "date": "2026-03-25",
                  "quantity": 10.0
                }
                """.formatted(animalId, feedTypeId);
    }
}
