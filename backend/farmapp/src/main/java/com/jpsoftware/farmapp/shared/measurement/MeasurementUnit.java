package com.jpsoftware.farmapp.shared.measurement;

import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.util.Locale;
import org.springframework.util.StringUtils;

public enum MeasurementUnit {
    LITER("L", 1.0),
    MILLILITER("mL", 1_000.0),
    KILOGRAM("kg", 1.0),
    GRAM("g", 1_000.0);

    private final String symbol;
    private final double unitsPerBaseUnit;

    MeasurementUnit(String symbol, double unitsPerBaseUnit) {
        this.symbol = symbol;
        this.unitsPerBaseUnit = unitsPerBaseUnit;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getUnitsPerBaseUnit() {
        return unitsPerBaseUnit;
    }

    public static MeasurementUnit fromProductionParam(String value, String parameterName) {
        if (!StringUtils.hasText(value)) {
            return LITER;
        }

        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "LITER" -> LITER;
            case "MILLILITER" -> MILLILITER;
            default -> throw new ValidationException(parameterName + " must be LITER or MILLILITER");
        };
    }

    public static MeasurementUnit fromFeedingParam(String value, String parameterName) {
        if (!StringUtils.hasText(value)) {
            return KILOGRAM;
        }

        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "KILOGRAM" -> KILOGRAM;
            case "GRAM" -> GRAM;
            default -> throw new ValidationException(parameterName + " must be KILOGRAM or GRAM");
        };
    }
}
