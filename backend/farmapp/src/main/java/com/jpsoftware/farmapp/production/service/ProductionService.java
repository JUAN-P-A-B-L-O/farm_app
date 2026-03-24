package com.jpsoftware.farmapp.production.service;

import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import com.jpsoftware.farmapp.production.repository.ProductionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductionService {

    private final ProductionRepository productionRepository;

    public ProductionService(ProductionRepository productionRepository) {
        this.productionRepository = productionRepository;
    }

    @Transactional
    public ProductionResponse create(CreateProductionRequest request) {
        validateInput(request);

        ProductionEntity productionEntity = toEntity(request);
        ProductionEntity savedProduction = productionRepository.save(productionEntity);

        return toResponse(savedProduction);
    }

    private void validateInput(CreateProductionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create production request must not be null");
        }
        if (!StringUtils.hasText(request.getAnimalId())) {
            throw new IllegalArgumentException("Production animalId must not be blank");
        }
        if (request.getDate() == null) {
            throw new IllegalArgumentException("Production date must not be null");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Production quantity must be greater than zero");
        }
    }

    private ProductionEntity toEntity(CreateProductionRequest request) {
        ProductionEntity productionEntity = new ProductionEntity();
        productionEntity.setId(UUID.randomUUID().toString());
        productionEntity.setAnimalId(request.getAnimalId());
        productionEntity.setDate(request.getDate());
        productionEntity.setQuantity(request.getQuantity());
        return productionEntity;
    }

    private ProductionResponse toResponse(ProductionEntity entity) {
        return new ProductionResponse(
                entity.getId(),
                entity.getAnimalId(),
                entity.getDate(),
                entity.getQuantity());
    }
}
