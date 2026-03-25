package com.jpsoftware.farmapp.feed.repository;

import com.jpsoftware.farmapp.feed.entity.FeedTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedTypeRepository extends JpaRepository<FeedTypeEntity, String> {
}
