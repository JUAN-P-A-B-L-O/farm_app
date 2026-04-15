package com.jpsoftware.farmapp.milkprice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Milk price entry or the currently resolved default milk price.")
public class MilkPriceResponse {

    @Schema(description = "Milk price identifier.", example = "price-001")
    private String id;

    @Schema(description = "Farm identifier.", example = "farm-001")
    private String farmId;

    @Schema(description = "Milk price per liter.", example = "2.35")
    private Double price;

    @Schema(description = "Date from which the price is effective.", example = "2026-04-14", nullable = true)
    private LocalDate effectiveDate;

    @Schema(description = "Timestamp when the record was created.", example = "2026-04-14T09:30:00", nullable = true)
    private LocalDateTime createdAt;

    @Schema(description = "User identifier that registered the price.", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
    private String createdBy;

    @Schema(description = "Indicates whether the response is using the legacy fallback price.", example = "false")
    private boolean fallbackDefault;

    public MilkPriceResponse() {
    }

    public MilkPriceResponse(
            String id,
            String farmId,
            Double price,
            LocalDate effectiveDate,
            LocalDateTime createdAt,
            String createdBy,
            boolean fallbackDefault) {
        this.id = id;
        this.farmId = farmId;
        this.price = price;
        this.effectiveDate = effectiveDate;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.fallbackDefault = fallbackDefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isFallbackDefault() {
        return fallbackDefault;
    }

    public void setFallbackDefault(boolean fallbackDefault) {
        this.fallbackDefault = fallbackDefault;
    }
}
