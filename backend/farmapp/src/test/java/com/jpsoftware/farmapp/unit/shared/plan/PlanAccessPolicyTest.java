package com.jpsoftware.farmapp.unit.shared.plan;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.shared.plan.PlanAccessDecision;
import com.jpsoftware.farmapp.shared.plan.PlanAccessDeniedException;
import com.jpsoftware.farmapp.shared.plan.PlanAccessPolicy;
import com.jpsoftware.farmapp.shared.plan.PlanFeature;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.junit.jupiter.api.Test;

class PlanAccessPolicyTest {

    private final PlanAccessPolicy planAccessPolicy = new PlanAccessPolicy();

    @Test
    void shouldDenyPremiumFeatureForFreePlan() {
        assertFalse(planAccessPolicy.hasAccess(UserPlan.FREE, PlanFeature.DASHBOARD));
        assertThrows(
                PlanAccessDeniedException.class,
                () -> planAccessPolicy.assertHasAccess(UserPlan.FREE, PlanFeature.CSV_EXPORT));
    }

    @Test
    void shouldAllowPremiumFeatureForProPlan() {
        assertTrue(planAccessPolicy.hasAccess(UserPlan.PRO, PlanFeature.DASHBOARD));
        assertDoesNotThrow(() -> planAccessPolicy.assertHasAccess(UserPlan.PRO, PlanFeature.ANALYTICS));
    }

    @Test
    void shouldResolveDefaultPlanWhenPlanIsMissing() {
        PlanAccessDecision decision = planAccessPolicy.evaluate(null, PlanFeature.CSV_EXPORT);

        assertEquals(UserPlan.FREE, decision.currentPlan());
        assertEquals(UserPlan.PRO, decision.minimumPlan());
        assertFalse(decision.allowed());
    }
}
