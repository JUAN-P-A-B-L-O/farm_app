package com.jpsoftware.farmapp.user.repository;

import com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFarmAssignmentRepository extends JpaRepository<UserFarmAssignmentEntity, UUID> {

    boolean existsByUserIdAndFarmId(UUID userId, String farmId);

    List<UserFarmAssignmentEntity> findByUserId(UUID userId);
}
