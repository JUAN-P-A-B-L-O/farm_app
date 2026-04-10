package com.jpsoftware.farmapp.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AnalyticsProfitPointResponse {

    @Schema(description = "Aggregation period label.", example = "2026-04-01")
    private final String period;

    @Schema(description = "Aggregated production quantity for the period.", example = "120.5")
    private final Double production;

    @Schema(description = "Aggregated feeding cost for the period.", example = "80.75")
    private final Double feedingCost;

    @Schema(description = "Aggregated revenue for the period.", example = "241.0")
    private final Double revenue;

    @Schema(description = "Aggregated profit for the period.", example = "160.25")
    private final Double profit;

    public AnalyticsProfitPointResponse(
            String period,
            Double production,
            Double feedingCost,
            Double revenue,
            Double profit) {
        this.period = period;
        this.production = production;
        this.feedingCost = feedingCost;
        this.revenue = revenue;
        this.profit = profit;
    }

    public String getPeriod() {
        return period;
    }

    public Double getProduction() {
        return production;
    }

    public Double getFeedingCost() {
        return feedingCost;
    }

    public Double getRevenue() {
        return revenue;
    }

    public Double getProfit() {
        return profit;
    }
}
