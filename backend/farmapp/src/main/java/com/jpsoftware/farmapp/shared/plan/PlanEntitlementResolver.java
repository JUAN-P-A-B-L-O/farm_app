package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.springframework.stereotype.Service;

@Service
public class PlanEntitlementResolver {

    public PlanEntitlement resolve(UserPlan plan) {
        return PlanEntitlement.internallyActivated(plan == null ? UserPlan.defaultPlan() : plan);
    }
}
