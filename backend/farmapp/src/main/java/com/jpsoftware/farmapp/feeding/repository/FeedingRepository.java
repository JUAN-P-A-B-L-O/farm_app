package com.jpsoftware.farmapp.feeding.repository;

import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface FeedingRepository extends JpaRepository<FeedingEntity, String> {

    List<FeedingEntity> findByAnimalId(String animalId);

    List<FeedingEntity> findByDate(LocalDate date);

    List<FeedingEntity> findByAnimalIdAndDate(String animalId, LocalDate date);

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
