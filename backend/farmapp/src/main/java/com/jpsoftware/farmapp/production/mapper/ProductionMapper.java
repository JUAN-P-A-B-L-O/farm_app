package com.jpsoftware.farmapp.production.mapper;

import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
import com.jpsoftware.farmapp.production.dto.ProductionResponse;
import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductionMapper {

    public ProductionEntity toEntity(CreateProductionRequest request) {
        ProductionEntity productionEntity = new ProductionEntity();
        productionEntity.setAnimalId(request.getAnimalId());
        productionEntity.setDate(request.getDate());
        productionEntity.setQuantity(request.getQuantity());
        return productionEntity;
    }

    public ProductionResponse toResponse(ProductionEntity entity) {
        return new ProductionResponse(
                entity.getId(),
                entity.getAnimalId(),
                entity.getDate(),
                entity.getQuantity()
        );
    }

    public ProductionResponse toResponse(ProductionEntity entity, AnimalSummaryResponse animal) {
        return new ProductionResponse(
                entity.getId(),
                entity.getAnimalId(),
                entity.getDate(),
                entity.getQuantity(),
                animal
        );
    }
}
