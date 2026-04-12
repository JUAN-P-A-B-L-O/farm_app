package com.jpsoftware.farmapp.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "productions")
public class ProductionEntity {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "animal_id", nullable = false)
    private String animalId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "farm_id")
    private String farmId;

    @Column(nullable = false)
    private String status;

    public ProductionEntity() {
        this.status = STATUS_ACTIVE;
    }

    public ProductionEntity(String id, String animalId, LocalDate date, Double quantity, String createdBy) {
        this(id, animalId, date, quantity, createdBy, STATUS_ACTIVE);
    }

    public ProductionEntity(String id, String animalId, LocalDate date, Double quantity, String createdBy, String status) {
        this(id, animalId, date, quantity, createdBy, null, status);
    }

    public ProductionEntity(String id, String animalId, LocalDate date, Double quantity, String createdBy, String farmId, String status) {
        this.id = id;
        this.animalId = animalId;
        this.date = date;
        this.quantity = quantity;
        this.createdBy = createdBy;
        this.farmId = farmId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
