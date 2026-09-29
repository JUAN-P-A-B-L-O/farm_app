package com.jpsoftware.farmapp.shared.plan;

public class PlanAccessDeniedException extends RuntimeException {

    public PlanAccessDeniedException(String message) {
        super(message);
    }
}
