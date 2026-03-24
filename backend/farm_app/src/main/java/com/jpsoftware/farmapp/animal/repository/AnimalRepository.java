package com.jpsoftware.farmapp.animal.repository;

import com.jpsoftware.farmapp.animal.entity.AnimalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<AnimalEntity, String> {

    List<AnimalEntity> findByFarmId(String farmId);
}
