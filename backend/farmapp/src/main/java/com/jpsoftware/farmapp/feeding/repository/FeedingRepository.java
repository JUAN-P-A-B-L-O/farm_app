package com.jpsoftware.farmapp.feeding.repository;

import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface FeedingRepository extends JpaRepository<FeedingEntity, String> {

    @Query("""
            SELECT COALESCE(SUM(f.quantity * ft.costPerKg), 0)
            FROM FeedingEntity f
            JOIN FeedTypeEntity ft ON f.feedTypeId = ft.id
            WHERE f.animalId = :animalId
            """)
    Double sumFeedingCostByAnimalId(@Param("animalId") String animalId);
}
