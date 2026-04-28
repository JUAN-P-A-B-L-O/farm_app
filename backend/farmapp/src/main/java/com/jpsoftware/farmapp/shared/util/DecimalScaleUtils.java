package com.jpsoftware.farmapp.shared.util;

import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DecimalScaleUtils {

    private static final int SCALE = 2;

    private DecimalScaleUtils() {
    }

    public static void requireMaxScale(Double value, String fieldName) {
        if (value == null) {
            return;
        }

        if (BigDecimal.valueOf(value).stripTrailingZeros().scale() > SCALE) {
            throw new ValidationException(fieldName + " must have at most 2 decimal places");
        }
    }

    public static Double normalize(Double value) {
        return normalize(value, SCALE);
    }

    public static Double normalize(Double value, int scale) {
        if (value == null) {
            return null;
        }

        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static Double zeroIfNull(Double value) {
        return normalize(value != null ? value : 0.0);
    }

    public static Double multiply(Double left, Double right) {
        return multiply(left, right, SCALE);
    }

    public static Double multiply(Double left, Double right, int scale) {
        return BigDecimal.valueOf(left != null ? left : 0.0)
                .multiply(BigDecimal.valueOf(right != null ? right : 0.0))
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static Double subtract(Double left, Double right) {
        return BigDecimal.valueOf(left != null ? left : 0.0)
                .subtract(BigDecimal.valueOf(right != null ? right : 0.0))
                .setScale(SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
