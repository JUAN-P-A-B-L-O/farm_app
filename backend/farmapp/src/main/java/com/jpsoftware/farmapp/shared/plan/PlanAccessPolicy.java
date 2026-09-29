package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.springframework.stereotype.Service;

@Service
public class PlanAccessPolicy {

    private final PlanEntitlementResolver planEntitlementResolver;

    public PlanAccessPolicy(PlanEntitlementResolver planEntitlementResolver) {
        this.planEntitlementResolver = planEntitlementResolver;
    }

    public PlanAccessDecision evaluate(UserPlan plan, PlanFeature feature) {
        return evaluate(planEntitlementResolver.resolve(plan), feature);
    }

    public PlanAccessDecision evaluate(PlanEntitlement entitlement, PlanFeature feature) {
        PlanEntitlement resolvedEntitlement = entitlement == null
                ? PlanEntitlement.defaultEntitlement()
                : entitlement;
        if (feature == null) {
            return new PlanAccessDecision(
                    resolvedEntitlement.plan(),
                    resolvedEntitlement.activationStatus(),
                    resolvedEntitlement.activationSource(),
                    null,
                    UserPlan.defaultPlan(),
                    true);
        }

        return new PlanAccessDecision(
                resolvedEntitlement.plan(),
                resolvedEntitlement.activationStatus(),
                resolvedEntitlement.activationSource(),
                feature,
                feature.getMinimumPlan(),
                feature.isAvailableFor(resolvedEntitlement));
    }

    public boolean hasAccess(UserPlan plan, PlanFeature feature) {
        return evaluate(plan, feature).allowed();
    }

    public boolean hasAccess(PlanEntitlement entitlement, PlanFeature feature) {
        return evaluate(entitlement, feature).allowed();
    }

    public void assertHasAccess(UserPlan plan, PlanFeature feature) {
        assertHasAccess(planEntitlementResolver.resolve(plan), feature);
    }

    public void assertHasAccess(PlanEntitlement entitlement, PlanFeature feature) {
        PlanAccessDecision decision = evaluate(entitlement, feature);
        if (!decision.allowed()) {
            throw new PlanAccessDeniedException("This feature requires the PRO plan");
        }
    }
}
