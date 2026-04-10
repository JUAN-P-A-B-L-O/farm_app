package com.jpsoftware.farmapp.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AnalyticsTimeSeriesPointResponse {

    @Schema(description = "Aggregation period label.", example = "2026-04-01")
    private final String period;

    @Schema(description = "Aggregated value for the period.", example = "120.5")
    private final Double value;

    public AnalyticsTimeSeriesPointResponse(String period, Double value) {
        this.period = period;
        this.value = value;
    }

    public String getPeriod() {
        return period;
    }

    public Double getValue() {
        return value;
    }
}
