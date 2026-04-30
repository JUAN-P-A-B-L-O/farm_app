package com.jpsoftware.farmapp.animalbatch.mapper;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
import com.jpsoftware.farmapp.animalbatch.entity.AnimalBatchEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AnimalBatchMapper {

    public AnimalBatchEntity toEntity(CreateAnimalBatchRequest request) {
        AnimalBatchEntity entity = new AnimalBatchEntity();
        entity.setName(request.getName());
        return entity;
    }

    public AnimalBatchResponse toResponse(AnimalBatchEntity entity, List<AnimalSummaryResponse> animals) {
        return new AnimalBatchResponse(
                entity.getId(),
                entity.getName(),
                entity.getFarmId(),
                animals);
    }
}
