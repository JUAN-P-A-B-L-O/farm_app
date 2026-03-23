package com.juan.farmapp.animal.service;

import com.juan.farmapp.animal.dto.AnimalResponse;
import com.juan.farmapp.animal.dto.CreateAnimalRequest;
import com.juan.farmapp.animal.entity.AnimalEntity;
import com.juan.farmapp.animal.mapper.AnimalMapper;
import com.juan.farmapp.animal.repository.AnimalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;

    public AnimalService(AnimalRepository animalRepository, AnimalMapper animalMapper) {
        this.animalRepository = animalRepository;
        this.animalMapper = animalMapper;
    }

    @Transactional
    public AnimalResponse create(CreateAnimalRequest request) {
        validateInput(request);

        AnimalEntity animalEntity = animalMapper.toEntity(request);
        AnimalEntity savedAnimal = animalRepository.save(animalEntity);

        return animalMapper.toResponse(savedAnimal);
    }

    @Transactional(readOnly = true)
    public AnimalResponse findById(String id) {
        AnimalEntity animalEntity = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal not found"));

        return animalMapper.toResponse(animalEntity);
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> findAll(String farmId) {
        List<AnimalEntity> animals = StringUtils.hasText(farmId)
                ? animalRepository.findByFarmId(farmId)
                : animalRepository.findAll();

        return animals.stream()
                .map(animalMapper::toResponse)
                .toList();
    }

    private void validateInput(CreateAnimalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create animal request must not be null");
        }
        if (!StringUtils.hasText(request.getTag())) {
            throw new IllegalArgumentException("Animal tag must not be blank");
        }
        if (!StringUtils.hasText(request.getBreed())) {
            throw new IllegalArgumentException("Animal breed must not be blank");
        }
        if (!StringUtils.hasText(request.getFarmId())) {
            throw new IllegalArgumentException("Animal farmId must not be blank");
        }
    }
}
