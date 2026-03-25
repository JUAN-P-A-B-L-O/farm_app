package com.jpsoftware.farmapp.feeding.mapper;

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
        entity.setCreatedBy(request.getUserId());
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
}
