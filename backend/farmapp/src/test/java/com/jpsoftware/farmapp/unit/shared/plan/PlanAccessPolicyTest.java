package com.jpsoftware.farmapp.unit.shared.plan;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.shared.plan.PlanAccessDecision;
import com.jpsoftware.farmapp.shared.plan.PlanAccessDeniedException;
import com.jpsoftware.farmapp.shared.plan.PlanActivationSource;
import com.jpsoftware.farmapp.shared.plan.PlanActivationStatus;
import com.jpsoftware.farmapp.shared.plan.PlanAccessPolicy;
import com.jpsoftware.farmapp.shared.plan.PlanEntitlement;
import com.jpsoftware.farmapp.shared.plan.PlanEntitlementResolver;
import com.jpsoftware.farmapp.shared.plan.PlanFeature;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.junit.jupiter.api.Test;

class PlanAccessPolicyTest {

    private final PlanAccessPolicy planAccessPolicy = new PlanAccessPolicy(new PlanEntitlementResolver());

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
        PlanAccessDecision decision = planAccessPolicy.evaluate((UserPlan) null, PlanFeature.CSV_EXPORT);

        assertEquals(UserPlan.FREE, decision.currentPlan());
        assertEquals(PlanActivationStatus.ACTIVE, decision.activationStatus());
        assertEquals(PlanActivationSource.INTERNAL_DEFAULT, decision.activationSource());
        assertEquals(PlanFeature.CSV_EXPORT, decision.feature());
        assertEquals(UserPlan.PRO, decision.minimumPlan());
        assertFalse(decision.allowed());
    }

    @Test
    void shouldAllowWhenFeatureIsMissing() {
        PlanAccessDecision decision = planAccessPolicy.evaluate((UserPlan) null, null);

        assertEquals(UserPlan.FREE, decision.currentPlan());
        assertEquals(PlanActivationStatus.ACTIVE, decision.activationStatus());
        assertEquals(PlanActivationSource.INTERNAL_DEFAULT, decision.activationSource());
        assertNull(decision.feature());
        assertEquals(UserPlan.FREE, decision.minimumPlan());
        assertTrue(decision.allowed());
    }

    @Test
    void shouldDenyProFeatureUntilExternalConfirmationActivatesEntitlement() {
        PlanEntitlement entitlement = PlanEntitlement.pendingExternalConfirmation(UserPlan.PRO, "sub_123");

        PlanAccessDecision decision = planAccessPolicy.evaluate(entitlement, PlanFeature.DASHBOARD);

        assertEquals(UserPlan.PRO, decision.currentPlan());
        assertEquals(PlanActivationStatus.PENDING_EXTERNAL_CONFIRMATION, decision.activationStatus());
        assertEquals(PlanActivationSource.EXTERNAL_PROVIDER, decision.activationSource());
        assertFalse(decision.allowed());
        assertThrows(
                PlanAccessDeniedException.class,
                () -> planAccessPolicy.assertHasAccess(entitlement, PlanFeature.CSV_EXPORT));
    }

    @Test
    void shouldDenyCanceledEntitlementEvenForPaidPlanTier() {
        PlanEntitlement entitlement = PlanEntitlement.canceled(UserPlan.PRO, "sub_456");

        assertFalse(planAccessPolicy.hasAccess(entitlement, PlanFeature.ANALYTICS));
    }
}
