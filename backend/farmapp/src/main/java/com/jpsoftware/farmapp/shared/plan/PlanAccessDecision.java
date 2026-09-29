package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;

public record PlanAccessDecision(
        UserPlan currentPlan,
        PlanActivationStatus activationStatus,
        PlanActivationSource activationSource,
        PlanFeature feature,
        UserPlan minimumPlan,
        boolean allowed) {
}
