package com.jpsoftware.farmapp.feeding.mapper;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.feed.dto.FeedTypeSummaryResponse;
import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import org.springframework.stereotype.Component;

@Component
public class FeedingMapper {

    public FeedingEntity toEntity(CreateFeedingRequest request) {
        FeedingEntity entity = new FeedingEntity();
        entity.setAnimalId(request.getAnimalId());
        entity.setFeedTypeId(request.getFeedTypeId());
        entity.setDate(request.getDate());
        entity.setQuantity(request.getQuantity());
        return entity;
    }

    public FeedingResponse toResponse(FeedingEntity entity) {
        return new FeedingResponse(
                entity.getId(),
                entity.getAnimalId(),
                entity.getFeedTypeId(),
                entity.getDate(),
                entity.getQuantity());
    }

    public FeedingResponse toResponse(
            FeedingEntity entity,
            AnimalSummaryResponse animal,
            FeedTypeSummaryResponse feedType) {
        return new FeedingResponse(
                entity.getId(),
                entity.getAnimalId(),
                entity.getFeedTypeId(),
                entity.getDate(),
                entity.getQuantity(),
                animal,
                feedType);
    }
}
