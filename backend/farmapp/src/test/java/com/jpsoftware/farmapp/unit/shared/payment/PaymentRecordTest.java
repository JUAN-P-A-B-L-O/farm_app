package com.jpsoftware.farmapp.unit.shared.payment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.shared.payment.PaymentProvider;
import com.jpsoftware.farmapp.shared.payment.PaymentRecord;
import com.jpsoftware.farmapp.shared.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

class PaymentRecordTest {

    @Test
    void shouldReportConfirmedPaymentAsConfirmedAndTerminal() {
        PaymentRecord paymentRecord = new PaymentRecord(
                PaymentProvider.STRIPE,
                PaymentStatus.CONFIRMED,
                "sub_123",
                null,
                null);

        assertTrue(paymentRecord.isConfirmed());
        assertTrue(paymentRecord.isTerminal());
    }

    @Test
    void shouldTreatOnlyPendingPaymentAsNonTerminal() {
        PaymentRecord pendingPayment = new PaymentRecord(
                PaymentProvider.MANUAL,
                PaymentStatus.PENDING,
                "manual_123",
                null,
                null);
        PaymentRecord failedPayment = new PaymentRecord(
                PaymentProvider.MANUAL,
                PaymentStatus.FAILED,
                "manual_124",
                null,
                null);
        PaymentRecord canceledPayment = new PaymentRecord(
                PaymentProvider.INTERNAL,
                PaymentStatus.CANCELED,
                "internal_125",
                null,
                null);
        PaymentRecord refundedPayment = new PaymentRecord(
                PaymentProvider.STRIPE,
                PaymentStatus.REFUNDED,
                "sub_126",
                null,
                null);

        assertFalse(pendingPayment.isConfirmed());
        assertFalse(pendingPayment.isTerminal());
        assertTrue(failedPayment.isTerminal());
        assertTrue(canceledPayment.isTerminal());
        assertTrue(refundedPayment.isTerminal());
    }
}
