package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;

public enum PlanFeature {
    DASHBOARD(UserPlan.PRO),
    ANALYTICS(UserPlan.PRO),
    CSV_EXPORT(UserPlan.PRO);

    private final UserPlan minimumPlan;

    PlanFeature(UserPlan minimumPlan) {
        this.minimumPlan = minimumPlan;
    }

    public UserPlan getMinimumPlan() {
        return minimumPlan;
    }
}
