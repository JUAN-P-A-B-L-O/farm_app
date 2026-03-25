package com.jpsoftware.farmapp.feed.mapper;

import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import org.springframework.stereotype.Component;

@Component
public class FeedTypeMapper {

    public FeedTypeEntity toEntity(CreateFeedTypeRequest request) {
        FeedTypeEntity entity = new FeedTypeEntity();
        entity.setName(request.getName());
        entity.setCostPerKg(request.getCostPerKg());
        entity.setActive(Boolean.TRUE);
        return entity;
    }

    public FeedTypeResponse toResponse(FeedTypeEntity entity) {
        return new FeedTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getCostPerKg(),
                entity.getActive());
    }
}
