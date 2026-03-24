package com.jpsoftware.farmapp.animal.mapper;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AnimalMapper {

    public AnimalEntity toEntity(CreateAnimalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create animal request must not be null");
        }

        return AnimalEntity.builder()
                .id(UUID.randomUUID().toString())
                .tag(request.getTag())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .status("ACTIVE")
                .farmId(request.getFarmId())
                .build();
    }

    public AnimalResponse toResponse(AnimalEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Animal entity must not be null");
        }

        return AnimalResponse.builder()
                .id(entity.getId())
                .tag(entity.getTag())
                .breed(entity.getBreed())
                .birthDate(entity.getBirthDate())
                .status(entity.getStatus())
                .farmId(entity.getFarmId())
                .build();
    }
}
