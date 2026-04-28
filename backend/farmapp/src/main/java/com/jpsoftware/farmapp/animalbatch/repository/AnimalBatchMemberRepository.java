package com.jpsoftware.farmapp.animalbatch.repository;

import com.jpsoftware.farmapp.animalbatch.entity.AnimalBatchMemberEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalBatchMemberRepository extends JpaRepository<AnimalBatchMemberEntity, String> {

    List<AnimalBatchMemberEntity> findByBatchId(String batchId);

    List<AnimalBatchMemberEntity> findByBatchIdIn(Collection<String> batchIds);

    void deleteByBatchId(String batchId);
}
