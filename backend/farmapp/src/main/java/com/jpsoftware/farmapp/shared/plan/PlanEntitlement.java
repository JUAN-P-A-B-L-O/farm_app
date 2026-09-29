package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;

public record PlanEntitlement(
        UserPlan plan,
        PlanActivationStatus activationStatus,
        PlanActivationSource activationSource,
        String externalReference) {

    public PlanEntitlement {
        plan = plan == null ? UserPlan.defaultPlan() : plan;
        activationStatus = activationStatus == null ? PlanActivationStatus.ACTIVE : activationStatus;
        activationSource = activationSource == null ? PlanActivationSource.INTERNAL_DEFAULT : activationSource;
    }

    public static PlanEntitlement defaultEntitlement() {
        return internallyActivated(UserPlan.defaultPlan());
    }

    public static PlanEntitlement internallyActivated(UserPlan plan) {
        return new PlanEntitlement(plan, PlanActivationStatus.ACTIVE, PlanActivationSource.INTERNAL_DEFAULT, null);
    }

    public static PlanEntitlement pendingExternalConfirmation(UserPlan plan, String externalReference) {
        return new PlanEntitlement(
                plan,
                PlanActivationStatus.PENDING_EXTERNAL_CONFIRMATION,
                PlanActivationSource.EXTERNAL_PROVIDER,
                externalReference);
    }

    public static PlanEntitlement canceled(UserPlan plan, String externalReference) {
        return new PlanEntitlement(plan, PlanActivationStatus.CANCELED, PlanActivationSource.EXTERNAL_PROVIDER, externalReference);
    }

    public boolean isActive() {
        return activationStatus == PlanActivationStatus.ACTIVE;
    }

    public boolean includes(UserPlan requiredPlan) {
        return plan.includes(requiredPlan);
    }

    public boolean allows(PlanFeature feature) {
        return feature == null || (isActive() && includes(feature.getMinimumPlan()));
    }
}
