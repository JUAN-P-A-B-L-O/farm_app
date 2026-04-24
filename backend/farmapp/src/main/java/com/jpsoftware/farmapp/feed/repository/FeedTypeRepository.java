package com.jpsoftware.farmapp.feed.repository;

import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FeedTypeRepository extends JpaRepository<FeedTypeEntity, String>, JpaSpecificationExecutor<FeedTypeEntity> {

    List<FeedTypeEntity> findByActiveTrue();

    List<FeedTypeEntity> findByFarmIdAndActiveTrue(String farmId);

    Page<FeedTypeEntity> findByActiveTrue(Pageable pageable);

    Page<FeedTypeEntity> findByFarmIdAndActiveTrue(String farmId, Pageable pageable);

    java.util.Optional<FeedTypeEntity> findByIdAndActiveTrue(String id);

    java.util.Optional<FeedTypeEntity> findByIdAndFarmId(String id, String farmId);

    java.util.Optional<FeedTypeEntity> findByIdAndFarmIdAndActiveTrue(String id, String farmId);

    boolean existsByIdAndFarmId(String id, String farmId);
}
