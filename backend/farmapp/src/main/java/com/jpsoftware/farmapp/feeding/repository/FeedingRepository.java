package com.jpsoftware.farmapp.feeding.repository;

import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedingRepository extends JpaRepository<FeedingEntity, String> {

    @Override
    @Query("SELECT f FROM FeedingEntity f WHERE f.status = 'ACTIVE'")
    List<FeedingEntity> findAll();

    @Override
    @Query("SELECT f FROM FeedingEntity f WHERE f.status = 'ACTIVE'")
    Page<FeedingEntity> findAll(Pageable pageable);

    @Override
    @Query("SELECT f FROM FeedingEntity f WHERE f.id = :id AND f.status = 'ACTIVE'")
    java.util.Optional<FeedingEntity> findById(@Param("id") String id);

    List<FeedingEntity> findByAnimalIdAndStatus(String animalId, String status);

    Page<FeedingEntity> findByAnimalIdAndStatus(String animalId, String status, Pageable pageable);

    List<FeedingEntity> findByDateAndStatus(LocalDate date, String status);

    Page<FeedingEntity> findByDateAndStatus(LocalDate date, String status, Pageable pageable);

    List<FeedingEntity> findByAnimalIdAndDateAndStatus(String animalId, LocalDate date, String status);

    Page<FeedingEntity> findByAnimalIdAndDateAndStatus(String animalId, LocalDate date, String status, Pageable pageable);

    @Query("SELECT f FROM FeedingEntity f WHERE f.id = :id")
    java.util.Optional<FeedingEntity> findAnyById(@Param("id") String id);

    @Query("""
            SELECT COALESCE(SUM(f.quantity * ft.costPerKg), 0)
            FROM FeedingEntity f
            JOIN FeedTypeEntity ft ON f.feedTypeId = ft.id
            WHERE f.animalId = :animalId
            AND f.status = 'ACTIVE'
            """)
    Double sumFeedingCostByAnimalId(@Param("animalId") String animalId);

    @Query("""
            SELECT COALESCE(SUM(f.quantity * ft.costPerKg), 0)
            FROM FeedingEntity f
            JOIN FeedTypeEntity ft ON f.feedTypeId = ft.id
            WHERE f.status = 'ACTIVE'
            """)
    Double sumTotalFeedingCost();
}
