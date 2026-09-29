package com.jpsoftware.farmapp.user.entity;

public enum UserPlan {
    FREE(0),
    PRO(1);

    private final int rank;

    UserPlan(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean includes(UserPlan requiredPlan) {
        UserPlan resolvedRequiredPlan = requiredPlan == null ? defaultPlan() : requiredPlan;
        return rank >= resolvedRequiredPlan.rank;
    }

    public static UserPlan defaultPlan() {
        return FREE;
    }
}
