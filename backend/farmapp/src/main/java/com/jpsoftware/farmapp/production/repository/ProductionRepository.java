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

    @Override
    @Query("SELECT p FROM ProductionEntity p WHERE p.status = 'ACTIVE'")
    List<ProductionEntity> findAll();

    @Override
    @Query("SELECT p FROM ProductionEntity p WHERE p.status = 'ACTIVE'")
    Page<ProductionEntity> findAll(Pageable pageable);

    @Override
    @Query("SELECT p FROM ProductionEntity p WHERE p.id = :id AND p.status = 'ACTIVE'")
    java.util.Optional<ProductionEntity> findById(@Param("id") String id);

    List<ProductionEntity> findByAnimalIdAndStatus(String animalId, String status);

    List<ProductionEntity> findByFarmIdAndStatus(String farmId, String status);

    Page<ProductionEntity> findByAnimalIdAndStatus(String animalId, String status, Pageable pageable);

    Page<ProductionEntity> findByFarmIdAndStatus(String farmId, String status, Pageable pageable);

    List<ProductionEntity> findByDateAndStatus(LocalDate date, String status);

    List<ProductionEntity> findByFarmIdAndDateAndStatus(String farmId, LocalDate date, String status);

    Page<ProductionEntity> findByDateAndStatus(LocalDate date, String status, Pageable pageable);

    Page<ProductionEntity> findByFarmIdAndDateAndStatus(String farmId, LocalDate date, String status, Pageable pageable);

    List<ProductionEntity> findByAnimalIdAndDateAndStatus(String animalId, LocalDate date, String status);

    List<ProductionEntity> findByFarmIdAndAnimalIdAndDateAndStatus(String farmId, String animalId, LocalDate date, String status);

    List<ProductionEntity> findByFarmIdAndAnimalIdAndStatus(String farmId, String animalId, String status);

    Page<ProductionEntity> findByAnimalIdAndDateAndStatus(String animalId, LocalDate date, String status, Pageable pageable);

    Page<ProductionEntity> findByFarmIdAndAnimalIdAndDateAndStatus(String farmId, String animalId, LocalDate date, String status, Pageable pageable);

    Page<ProductionEntity> findByFarmIdAndAnimalIdAndStatus(String farmId, String animalId, String status, Pageable pageable);

    java.util.Optional<ProductionEntity> findByIdAndFarmIdAndStatus(String id, String farmId, String status);

    @Query("SELECT p FROM ProductionEntity p WHERE p.id = :id")
    java.util.Optional<ProductionEntity> findAnyById(@Param("id") String id);

    @Query("SELECT p FROM ProductionEntity p WHERE p.id = :id AND p.farmId = :farmId")
    java.util.Optional<ProductionEntity> findAnyByIdAndFarmId(@Param("id") String id, @Param("farmId") String farmId);

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            WHERE p.animalId = :animalId
            AND p.status = 'ACTIVE'
            """)
    Double sumQuantityByAnimalId(@Param("animalId") String animalId);

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            WHERE p.animalId = :animalId
            AND p.status = 'ACTIVE'
            """)
    Double sumProductionByAnimalId(@Param("animalId") String animalId);

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            WHERE p.status = 'ACTIVE'
            """)
    Double sumTotalProduction();

    @Query("""
            SELECT COALESCE(SUM(p.quantity), 0)
            FROM ProductionEntity p
            WHERE p.farmId = :farmId
            AND p.status = 'ACTIVE'
            """)
    Double sumTotalProductionByFarmId(@Param("farmId") String farmId);
}
