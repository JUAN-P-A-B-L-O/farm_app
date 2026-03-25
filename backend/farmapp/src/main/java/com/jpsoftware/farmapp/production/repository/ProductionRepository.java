package com.jpsoftware.farmapp.production.repository;

import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionRepository extends JpaRepository<ProductionEntity, String> {

    List<ProductionEntity> findByAnimalId(String animalId);

    List<ProductionEntity> findByDate(LocalDate date);

    List<ProductionEntity> findByAnimalIdAndDate(String animalId, LocalDate date);

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            WHERE p.animalId = :animalId
            """)
    Double sumQuantityByAnimalId(@Param("animalId") String animalId);

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            WHERE p.animalId = :animalId
            """)
    Double sumProductionByAnimalId(@Param("animalId") String animalId);

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            """)
    Double sumTotalProduction();
}
