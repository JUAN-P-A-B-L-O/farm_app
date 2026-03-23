package com.juan.farmapp.animal.mapper;

import com.juan.farmapp.animal.dto.AnimalResponse;
import com.juan.farmapp.animal.dto.CreateAnimalRequest;
import com.juan.farmapp.animal.entity.AnimalEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AnimalMapper {

    public AnimalEntity toEntity(CreateAnimalRequest request) {
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
