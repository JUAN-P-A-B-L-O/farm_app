package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PlanEntitlementResolver {

    public PlanEntitlement resolve(UserPlan plan) {
        return PlanEntitlement.internallyActivated(plan == null ? UserPlan.defaultPlan() : plan);
    }

    public PlanEntitlement resolve(UserEntity user) {
        if (user == null) {
            return PlanEntitlement.defaultEntitlement();
        }

        BillingSubscriptionStatus billingStatus = user.getBillingSubscriptionStatus();
        if (billingStatus == null) {
            return resolve(user.getPlan());
        }

        String externalReference = StringUtils.hasText(user.getStripeSubscriptionId())
                ? user.getStripeSubscriptionId()
                : user.getStripeCustomerId();

        if (billingStatus.isPremiumActive()) {
            return PlanEntitlement.externallyActivated(UserPlan.PRO, externalReference);
        }
        if (billingStatus.isPendingActivation()) {
            return PlanEntitlement.pendingExternalConfirmation(UserPlan.PRO, externalReference);
        }
        if (billingStatus.isCanceled()) {
            return PlanEntitlement.canceled(UserPlan.FREE, externalReference);
        }
        return PlanEntitlement.externallyInactive(UserPlan.FREE, externalReference);
    }
}
