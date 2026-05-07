package com.jpsoftware.farmapp.shared.email.model;

public record EmailMessage(
        String recipientEmail,
        String subject,
        String body) {
}
