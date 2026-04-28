package com.jpsoftware.farmapp.animalbatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "animal_batch_members")
public class AnimalBatchMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @Column(name = "animal_id", nullable = false)
    private String animalId;

    public AnimalBatchMemberEntity() {
    }

    public AnimalBatchMemberEntity(String id, String batchId, String animalId) {
        this.id = id;
        this.batchId = batchId;
        this.animalId = animalId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public void setAnimalId(String animalId) {
        this.animalId = animalId;
    }
}
