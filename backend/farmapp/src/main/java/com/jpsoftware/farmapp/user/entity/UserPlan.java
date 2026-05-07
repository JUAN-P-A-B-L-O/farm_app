package com.jpsoftware.farmapp.user.entity;

public enum UserPlan {
    FREE(0, false),
    PRO(1, true);

    private final int rank;
    private final boolean paid;

    UserPlan(int rank, boolean paid) {
        this.rank = rank;
        this.paid = paid;
    }

    public int getRank() {
        return rank;
    }

    public boolean isPaid() {
        return paid;
    }

    public boolean includes(UserPlan requiredPlan) {
        UserPlan resolvedRequiredPlan = requiredPlan == null ? defaultPlan() : requiredPlan;
        return rank >= resolvedRequiredPlan.rank;
    }

    public static UserPlan defaultPlan() {
        return FREE;
    }
}
