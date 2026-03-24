package com.jpsoftware.farmapp.production.repository;

import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionRepository extends JpaRepository<ProductionEntity, String> {

    List<ProductionEntity> findByAnimalId(String animalId);

    List<ProductionEntity> findByDate(LocalDate date);

    List<ProductionEntity> findByAnimalIdAndDate(String animalId, LocalDate date);
}
