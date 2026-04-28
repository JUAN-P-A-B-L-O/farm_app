package com.jpsoftware.farmapp.animalbatch.repository;

import com.jpsoftware.farmapp.animalbatch.entity.AnimalBatchEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnimalBatchRepository extends JpaRepository<AnimalBatchEntity, String>, JpaSpecificationExecutor<AnimalBatchEntity> {

    Optional<AnimalBatchEntity> findByIdAndFarmId(String id, String farmId);
}
