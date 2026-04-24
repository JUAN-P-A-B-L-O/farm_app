package com.jpsoftware.farmapp.animal.repository;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<AnimalEntity, String> {

    boolean existsByTag(String tag);

    List<AnimalEntity> findByFarmId(String farmId);

    Page<AnimalEntity> findByFarmId(String farmId, Pageable pageable);

    java.util.Optional<AnimalEntity> findByIdAndFarmId(String id, String farmId);

    boolean existsByIdAndFarmId(String id, String farmId);

    long countByFarmId(String farmId);
}
