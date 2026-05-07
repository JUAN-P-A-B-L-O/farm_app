package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.auth.infrastructure.EmailProperties;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailConfirmationSender;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SmtpEmailConfirmationSenderTest {

    @Test
    void shouldSendConfirmationEmailWithConfiguredMetadata() {
        EmailProperties emailProperties = buildEmailProperties();
        SentMessage sentMessage = new SentMessage();
        SmtpEmailConfirmationSender sender = new SmtpEmailConfirmationSender(emailProperties, (properties, fromAddress, recipientEmail, message) -> {
            sentMessage.fromAddress = fromAddress;
            sentMessage.recipientEmail = recipientEmail;
            sentMessage.message = message;
        });

        sender.sendConfirmationEmail("maria@farm.com", "Maria Silva", "http://localhost:5173/login?token=abc");

        assertEquals("no-reply@farmapp.local", sentMessage.fromAddress);
        assertEquals("maria@farm.com", sentMessage.recipientEmail);
        assertTrue(sentMessage.message.contains("Subject: Confirme sua conta no Farm App"));
        assertTrue(sentMessage.message.contains("Ola Maria Silva,"));
        assertTrue(sentMessage.message.contains("http://localhost:5173/login?token=abc"));
    }

    @Test
    void shouldWrapMailFailuresAsEmailDispatchException() {
        EmailProperties emailProperties = buildEmailProperties();
        SmtpEmailConfirmationSender sender = new SmtpEmailConfirmationSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new IOException("smtp error");
                });

        EmailDispatchException exception = assertThrows(
                EmailDispatchException.class,
                () -> sender.sendConfirmationEmail("maria@farm.com", "Maria Silva", "http://localhost:5173/login?token=abc"));

        assertEquals("Unable to send confirmation email", exception.getMessage());
    }

    private EmailProperties buildEmailProperties() {
        EmailProperties emailProperties = new EmailProperties();
        emailProperties.setFrom("no-reply@farmapp.local");
        emailProperties.getConfirmation().setSubject("Confirme sua conta no Farm App");
        emailProperties.setEnabled(true);
        emailProperties.getSmtp().setHost("smtp.example.com");
        emailProperties.getSmtp().setPort(587);
        emailProperties.getSmtp().setAuth(false);
        emailProperties.getSmtp().setStarttlsEnabled(false);
        return emailProperties;
    }

    private static final class SentMessage {
        private String fromAddress;
        private String recipientEmail;
        private String message;
    }
}
