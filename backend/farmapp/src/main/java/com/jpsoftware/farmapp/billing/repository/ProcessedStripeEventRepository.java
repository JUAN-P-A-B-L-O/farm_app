package com.jpsoftware.farmapp.billing.repository;

import com.jpsoftware.farmapp.billing.entity.ProcessedStripeEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedStripeEventRepository extends JpaRepository<ProcessedStripeEventEntity, String> {
}
