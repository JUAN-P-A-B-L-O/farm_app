package com.jpsoftware.farmapp.feeding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "feedings")
public class FeedingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String animalId;

    @Column(nullable = false)
    private String feedTypeId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double quantity;

    public FeedingEntity() {
    }

    public FeedingEntity(String id, String animalId, String feedTypeId, LocalDate date, Double quantity) {
        this.id = id;
        this.animalId = animalId;
        this.feedTypeId = feedTypeId;
        this.date = date;
        this.quantity = quantity;
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

    public String getFeedTypeId() {
        return feedTypeId;
    }

    public void setFeedTypeId(String feedTypeId) {
        this.feedTypeId = feedTypeId;
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
}
