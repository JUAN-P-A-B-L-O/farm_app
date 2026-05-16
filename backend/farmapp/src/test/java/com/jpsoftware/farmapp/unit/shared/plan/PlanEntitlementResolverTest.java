package com.jpsoftware.farmapp.unit.shared.plan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import com.jpsoftware.farmapp.shared.plan.PlanActivationSource;
import com.jpsoftware.farmapp.shared.plan.PlanActivationStatus;
import com.jpsoftware.farmapp.shared.plan.PlanEntitlement;
import com.jpsoftware.farmapp.shared.plan.PlanEntitlementResolver;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.junit.jupiter.api.Test;

class PlanEntitlementResolverTest {

    private final PlanEntitlementResolver resolver = new PlanEntitlementResolver();

    @Test
    void shouldResolveExternallyActivatedPremiumEntitlement() {
        UserEntity user = new UserEntity();
        user.setPlan(UserPlan.PRO);
        user.setStripeSubscriptionId("sub_123");
        user.setBillingSubscriptionStatus(BillingSubscriptionStatus.ACTIVE);

        PlanEntitlement entitlement = resolver.resolve(user);

        assertEquals(UserPlan.PRO, entitlement.plan());
        assertEquals(PlanActivationStatus.ACTIVE, entitlement.activationStatus());
        assertEquals(PlanActivationSource.EXTERNAL_PROVIDER, entitlement.activationSource());
        assertEquals("sub_123", entitlement.externalReference());
    }

    @Test
    void shouldResolvePendingPremiumEntitlementBeforeWebhookActivation() {
        UserEntity user = new UserEntity();
        user.setPlan(UserPlan.FREE);
        user.setStripeCustomerId("cus_123");
        user.setBillingSubscriptionStatus(BillingSubscriptionStatus.CHECKOUT_PENDING);

        PlanEntitlement entitlement = resolver.resolve(user);

        assertEquals(UserPlan.PRO, entitlement.plan());
        assertEquals(PlanActivationStatus.PENDING_EXTERNAL_CONFIRMATION, entitlement.activationStatus());
        assertEquals(PlanActivationSource.EXTERNAL_PROVIDER, entitlement.activationSource());
        assertEquals("cus_123", entitlement.externalReference());
    }

    @Test
    void shouldResolveCanceledEntitlementAsInactiveAccess() {
        UserEntity user = new UserEntity();
        user.setPlan(UserPlan.FREE);
        user.setStripeSubscriptionId("sub_456");
        user.setBillingSubscriptionStatus(BillingSubscriptionStatus.CANCELED);

        PlanEntitlement entitlement = resolver.resolve(user);

        assertEquals(UserPlan.FREE, entitlement.plan());
        assertEquals(PlanActivationStatus.CANCELED, entitlement.activationStatus());
        assertEquals(PlanActivationSource.EXTERNAL_PROVIDER, entitlement.activationSource());
        assertEquals("sub_456", entitlement.externalReference());
    }
}
