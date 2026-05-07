package com.jpsoftware.farmapp.auth.service;

public interface EmailConfirmationSender {

    void sendConfirmationEmail(String recipientEmail, String recipientName, String confirmationUrl);
}
