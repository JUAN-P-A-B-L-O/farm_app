package com.jpsoftware.farmapp.analytics.dto;

public enum AnalyticsGroupBy {
    DAY,
    MONTH;

    public static AnalyticsGroupBy fromQueryParam(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }

        return AnalyticsGroupBy.valueOf(value.trim().toUpperCase());
    }
}
