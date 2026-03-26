package com.jpsoftware.farmapp.animal.service;

import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import com.jpsoftware.farmapp.animal.mapper.AnimalMapper;
import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
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
        ensureTagIsUnique(request.getTag());

        AnimalEntity animalEntity = animalMapper.toEntity(request);
        AnimalEntity savedAnimal = animalRepository.save(animalEntity);

        return animalMapper.toResponse(savedAnimal);
    }

    @Transactional(readOnly = true)
    public AnimalResponse findById(String id) {
        AnimalEntity animalEntity = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));

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

    @Transactional
    public AnimalResponse update(String id, UpdateAnimalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update animal request must not be null");
        }

        AnimalEntity animalEntity = animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));

        validateTagUpdate(animalEntity, request);
        applyUpdates(animalEntity, request);

        AnimalEntity updatedAnimal = animalRepository.save(animalEntity);
        return animalMapper.toResponse(updatedAnimal);
    }

    @Transactional
    public void delete(String id) {
        animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal not found"));

        animalRepository.deleteById(id);
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

    private void applyUpdates(AnimalEntity animalEntity, UpdateAnimalRequest request) {
        if (request.getTag() != null) {
            animalEntity.setTag(request.getTag());
        }
        if (request.getBreed() != null) {
            animalEntity.setBreed(request.getBreed());
        }
        if (request.getBirthDate() != null) {
            animalEntity.setBirthDate(request.getBirthDate());
        }
        if (request.getStatus() != null) {
            animalEntity.setStatus(request.getStatus());
        }
        if (request.getFarmId() != null) {
            animalEntity.setFarmId(request.getFarmId());
        }
    }

    private void ensureTagIsUnique(String tag) {
        if (animalRepository.existsByTag(tag)) {
            throw new DataIntegrityViolationException("Animal with this tag already exists");
        }
    }

    private void validateTagUpdate(AnimalEntity animalEntity, UpdateAnimalRequest request) {
        if (request.getTag() == null) {
            return;
        }

        if (!request.getTag().equals(animalEntity.getTag())) {
            ensureTagIsUnique(request.getTag());
        }
    }
}
