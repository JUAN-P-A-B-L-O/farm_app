package com.jpsoftware.farmapp.shared.currency;

import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.util.Arrays;

public enum CurrencyCode {
    BRL(1.0),
    USD(0.20);

    private final double conversionRateFromBase;

    CurrencyCode(double conversionRateFromBase) {
        this.conversionRateFromBase = conversionRateFromBase;
    }

    public double getConversionRateFromBase() {
        return conversionRateFromBase;
    }

    public static CurrencyCode fromQueryParam(String value) {
        if (value == null || value.isBlank()) {
            return BRL;
        }

        return Arrays.stream(values())
                .filter(currency -> currency.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("currency must be BRL or USD"));
    }
}
