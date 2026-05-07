package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.springframework.stereotype.Service;

@Service
public class PlanAccessPolicy {

    public PlanAccessDecision evaluate(UserPlan plan, PlanFeature feature) {
        UserPlan resolvedPlan = plan == null ? UserPlan.defaultPlan() : plan;
        if (feature == null) {
            return new PlanAccessDecision(resolvedPlan, null, UserPlan.defaultPlan(), true);
        }

        return new PlanAccessDecision(
                resolvedPlan,
                feature,
                feature.getMinimumPlan(),
                feature.isAvailableFor(resolvedPlan));
    }

    public boolean hasAccess(UserPlan plan, PlanFeature feature) {
        return evaluate(plan, feature).allowed();
    }

    public void assertHasAccess(UserPlan plan, PlanFeature feature) {
        PlanAccessDecision decision = evaluate(plan, feature);
        if (!decision.allowed()) {
            throw new PlanAccessDeniedException("This feature requires the PRO plan");
        }
    }
}
