package com.jpsoftware.farmapp.farm.repository;

import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmRepository extends JpaRepository<FarmEntity, String> {

    List<FarmEntity> findByOwnerId(UUID ownerId);

    boolean existsByIdAndOwnerId(String id, UUID ownerId);
}
