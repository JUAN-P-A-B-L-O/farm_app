package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.springframework.stereotype.Service;

@Service
public class PlanAccessPolicy {

    public boolean hasAccess(UserPlan plan, PlanFeature feature) {
        if (feature == null) {
            return true;
        }

        UserPlan resolvedPlan = plan == null ? UserPlan.defaultPlan() : plan;
        return resolvedPlan.ordinal() >= feature.getMinimumPlan().ordinal();
    }

    public void assertHasAccess(UserPlan plan, PlanFeature feature) {
        if (!hasAccess(plan, feature)) {
            throw new PlanAccessDeniedException("This feature requires the PRO plan");
        }
    }
}
