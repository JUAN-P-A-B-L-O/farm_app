package com.jpsoftware.farmapp.feeding.repository;

import com.jpsoftware.farmapp.feeding.entity.FeedingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedingRepository extends JpaRepository<FeedingEntity, String> {
}
