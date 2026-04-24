package com.jpsoftware.farmapp.milkprice.repository;

import com.jpsoftware.farmapp.milkprice.entity.MilkPriceEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilkPriceRepository extends JpaRepository<MilkPriceEntity, String> {

    List<MilkPriceEntity> findByFarmIdOrderByEffectiveDateDescCreatedAtDesc(String farmId);

    Page<MilkPriceEntity> findByFarmIdOrderByEffectiveDateDescCreatedAtDesc(String farmId, Pageable pageable);

    Optional<MilkPriceEntity> findTopByFarmIdAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
            String farmId,
            LocalDate effectiveDate);
}
