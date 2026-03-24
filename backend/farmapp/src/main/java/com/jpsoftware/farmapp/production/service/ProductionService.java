package com.jpsoftware.farmapp.production.service;

import com.jpsoftware.farmapp.animal.repository.AnimalRepository;
import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.mapper.ProductionMapper;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import com.jpsoftware.farmapp.shared.exception.BusinessException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final AnimalRepository animalRepository;
    private final ProductionMapper productionMapper;

    public ProductionService(
            ProductionRepository productionRepository,
            AnimalRepository animalRepository,
            ProductionMapper productionMapper) {
        this.productionRepository = productionRepository;
        this.animalRepository = animalRepository;
        this.productionMapper = productionMapper;
    }

    @Transactional
    public ProductionResponse create(CreateProductionRequest request) {
        validateInput(request);

        ProductionEntity productionEntity = toEntity(request);
        ProductionEntity savedProduction = productionRepository.save(productionEntity);

        return productionMapper.toResponse(savedProduction);
    }

    @Transactional(readOnly = true)
    public List<ProductionResponse> findAll() {
        return productionRepository.findAll()
                .stream()
                .map(productionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductionResponse findById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ValidationException("id must not be blank");
        }

        ProductionEntity productionEntity = productionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production not found"));

        return productionMapper.toResponse(productionEntity);
    }

    private void validateInput(CreateProductionRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }
        if (!StringUtils.hasText(request.getAnimalId())) {
            throw new ValidationException("animalId must not be blank");
        }
        if (request.getDate() == null) {
            throw new ValidationException("date must not be null");
        }
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Date cannot be in the future");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new ValidationException("quantity must be greater than zero");
        }
        if (!animalRepository.existsById(request.getAnimalId())) {
            throw new ResourceNotFoundException("Animal not found");
        }
    }

    private ProductionEntity toEntity(CreateProductionRequest request) {
        return productionMapper.toEntity(request);
    }
}
