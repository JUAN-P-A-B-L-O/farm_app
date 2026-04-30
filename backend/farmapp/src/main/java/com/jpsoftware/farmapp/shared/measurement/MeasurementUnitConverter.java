package com.jpsoftware.farmapp.shared.measurement;

import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;

public final class MeasurementUnitConverter {

    private MeasurementUnitConverter() {
    }

    public static Double convertFromBase(Double value, MeasurementUnit unit) {
        if (value == null) {
            return null;
        }

        if (unit == MeasurementUnit.MILLILITER || unit == MeasurementUnit.GRAM) {
            return DecimalScaleUtils.normalize(value * unit.getUnitsPerBaseUnit());
        }

        return DecimalScaleUtils.normalize(value);
    }

    public static Double convertRateFromBase(Double value, MeasurementUnit unit, int scale) {
        if (value == null) {
            return null;
        }

        if (unit == MeasurementUnit.GRAM || unit == MeasurementUnit.MILLILITER) {
            return DecimalScaleUtils.normalize(value / unit.getUnitsPerBaseUnit(), scale);
        }

        return DecimalScaleUtils.normalize(value, scale);
    }
}
