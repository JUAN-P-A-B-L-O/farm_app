package com.jpsoftware.farmapp.shared.currency;

import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;

public final class CurrencyConversionUtils {

    private CurrencyConversionUtils() {
    }

    public static Double convertMonetaryValue(Double value, String currencyCode) {
        return convertMonetaryValue(value, currencyCode, 2);
    }

    public static Double convertMonetaryValue(Double value, String currencyCode, int scale) {
        if (value == null) {
            return null;
        }

        CurrencyCode currency = CurrencyCode.fromQueryParam(currencyCode);
        if (currency == CurrencyCode.BRL) {
            return DecimalScaleUtils.normalize(value, scale);
        }

        return DecimalScaleUtils.multiply(value, currency.getConversionRateFromBase(), scale);
    }

    public static String normalizeCurrencyCode(String currencyCode) {
        return CurrencyCode.fromQueryParam(currencyCode).name();
    }
}
