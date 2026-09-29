package com.jpsoftware.farmapp.shared.exception;

public class EmailConfirmationRequiredException extends RuntimeException {

    public EmailConfirmationRequiredException(String message) {
        super(message);
    }
}
