package com.jpsoftware.farmapp.shared.currency;

import com.jpsoftware.farmapp.shared.util.DecimalScaleUtils;

public final class CurrencyConversionUtils {

    private CurrencyConversionUtils() {
    }

    public static Double convertMonetaryValue(Double value, String currencyCode) {
        if (value == null) {
            return null;
        }

        CurrencyCode currency = CurrencyCode.fromQueryParam(currencyCode);
        if (currency == CurrencyCode.BRL) {
            return DecimalScaleUtils.normalize(value);
        }

        return DecimalScaleUtils.multiply(value, currency.getConversionRateFromBase());
    }

    public static String normalizeCurrencyCode(String currencyCode) {
        return CurrencyCode.fromQueryParam(currencyCode).name();
    }
}
