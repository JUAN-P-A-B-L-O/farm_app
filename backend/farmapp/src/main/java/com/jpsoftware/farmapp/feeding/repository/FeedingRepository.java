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

    List<FeedingEntity> findByAnimalId(String animalId);

    Page<FeedingEntity> findByAnimalId(String animalId, Pageable pageable);

    List<FeedingEntity> findByDate(LocalDate date);

    Page<FeedingEntity> findByDate(LocalDate date, Pageable pageable);

    List<FeedingEntity> findByAnimalIdAndDate(String animalId, LocalDate date);

    Page<FeedingEntity> findByAnimalIdAndDate(String animalId, LocalDate date, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(f.quantity * ft.costPerKg), 0)
            FROM FeedingEntity f
            JOIN FeedTypeEntity ft ON f.feedTypeId = ft.id
            WHERE f.animalId = :animalId
            """)
    Double sumFeedingCostByAnimalId(@Param("animalId") String animalId);

    @Query("""
            SELECT COALESCE(SUM(f.quantity * ft.costPerKg), 0)
            FROM FeedingEntity f
            JOIN FeedTypeEntity ft ON f.feedTypeId = ft.id
            """)
    Double sumTotalFeedingCost();
}
