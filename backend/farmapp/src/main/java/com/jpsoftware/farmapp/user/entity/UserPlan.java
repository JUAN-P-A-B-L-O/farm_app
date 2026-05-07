package com.jpsoftware.farmapp.user.entity;

public enum UserPlan {
    FREE(false),
    PRO(true);

    private final boolean paid;

    UserPlan(boolean paid) {
        this.paid = paid;
    }

    public boolean isPaid() {
        return paid;
    }

    public static UserPlan defaultPlan() {
        return FREE;
    }
}
