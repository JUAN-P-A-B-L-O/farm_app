package com.jpsoftware.farmapp.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
        name = "user_farm_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "farm_id"}))
public class UserFarmAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "farm_id", nullable = false)
    private String farmId;

    public UserFarmAssignmentEntity() {
    }

    public UserFarmAssignmentEntity(UUID id, UUID userId, String farmId) {
        this.id = id;
        this.userId = userId;
        this.farmId = farmId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }
}
