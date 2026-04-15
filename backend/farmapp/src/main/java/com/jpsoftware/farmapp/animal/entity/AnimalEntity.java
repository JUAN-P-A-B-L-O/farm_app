package com.jpsoftware.farmapp.animal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "animals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimalEntity {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_SOLD = "SOLD";
    public static final String STATUS_DEAD = "DEAD";
    public static final String ORIGIN_BORN = "BORN";
    public static final String ORIGIN_PURCHASED = "PURCHASED";

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String tag;

    @Column(nullable = false)
    private String breed;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String origin;

    private Double acquisitionCost;

    @Column(nullable = false)
    private String farmId;
}
