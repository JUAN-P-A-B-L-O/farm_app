package com.jpsoftware.farmapp.production.repository;

import com.jpsoftware.farmapp.production.entity.ProductionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionRepository extends JpaRepository<ProductionEntity, String> {

    List<ProductionEntity> findByAnimalId(String animalId);

    Page<ProductionEntity> findByAnimalId(String animalId, Pageable pageable);

    List<ProductionEntity> findByDate(LocalDate date);

    Page<ProductionEntity> findByDate(LocalDate date, Pageable pageable);

    List<ProductionEntity> findByAnimalIdAndDate(String animalId, LocalDate date);

    Page<ProductionEntity> findByAnimalIdAndDate(String animalId, LocalDate date, Pageable pageable);

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
