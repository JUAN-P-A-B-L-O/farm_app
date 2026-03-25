package com.jpsoftware.farmapp.feed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "feed_types")
public class FeedTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double costPerKg;

    @Column(nullable = false)
    private Boolean active;

    public FeedTypeEntity() {
    }

    public FeedTypeEntity(String id, String name, Double costPerKg, Boolean active) {
        this.id = id;
        this.name = name;
        this.costPerKg = costPerKg;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCostPerKg() {
        return costPerKg;
    }

    public void setCostPerKg(Double costPerKg) {
        this.costPerKg = costPerKg;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
