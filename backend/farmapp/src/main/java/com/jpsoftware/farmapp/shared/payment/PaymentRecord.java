package com.jpsoftware.farmapp.shared.payment;

import java.time.Instant;

public record PaymentRecord(
        PaymentProvider provider,
        PaymentStatus status,
        String externalReference,
        Instant createdAt,
        Instant confirmedAt) {

    public boolean isConfirmed() {
        return status == PaymentStatus.CONFIRMED;
    }

    public boolean isTerminal() {
        return status == PaymentStatus.CONFIRMED
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.CANCELED
                || status == PaymentStatus.REFUNDED;
    }
}
