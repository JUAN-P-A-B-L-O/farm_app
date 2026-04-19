package com.jpsoftware.farmapp.milkprice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Request payload for registering a new milk price.")
public class CreateMilkPriceRequest {

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2, message = "price must have at most 2 decimal places")
    @Schema(description = "Milk price per liter.", example = "2.35")
    private Double price;

    @NotNull
    @Schema(description = "Date from which the milk price becomes effective.", example = "2026-04-14")
    private LocalDate effectiveDate;

    public CreateMilkPriceRequest() {
    }

    public CreateMilkPriceRequest(Double price, LocalDate effectiveDate) {
        this.price = price;
        this.effectiveDate = effectiveDate;
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
}
