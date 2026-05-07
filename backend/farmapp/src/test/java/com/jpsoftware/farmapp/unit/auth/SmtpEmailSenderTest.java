package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.auth.infrastructure.EmailProperties;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class SmtpEmailSenderTest {

    @Test
    void shouldSendTransactionalEmailWithConfiguredMetadata() {
        EmailProperties emailProperties = buildEmailProperties();
        SentMessage sentMessage = new SentMessage();
        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, (properties, fromAddress, recipientEmail, message) -> {
            sentMessage.fromAddress = fromAddress;
            sentMessage.recipientEmail = recipientEmail;
            sentMessage.message = message;
        });

        sender.send(new EmailMessage(
                "  maria@farm.com  ",
                "Confirme sua conta no Farm App",
                "Olá Maria Silva,\n\nUse o link para confirmar."));

        assertEquals("no-reply@farmapp.local", sentMessage.fromAddress);
        assertEquals("maria@farm.com", sentMessage.recipientEmail);
        assertTrue(sentMessage.message.contains("Content-Type: text/plain; charset=UTF-8"));
        assertTrue(sentMessage.message.contains("Content-Transfer-Encoding: base64"));
        assertTrue(sentMessage.message.contains("=?UTF-8?B?"));
        assertTrue(decodeBody(sentMessage.message).contains("Olá Maria Silva,"));
    }

    @Test
    void shouldWrapMailFailuresAsEmailDispatchException() {
        EmailProperties emailProperties = buildEmailProperties();
        SmtpEmailSender sender = new SmtpEmailSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new IOException("smtp error");
                });

        EmailDispatchException exception = assertThrows(
                EmailDispatchException.class,
                () -> sender.send(new EmailMessage(
                        "maria@farm.com",
                        "Confirme sua conta no Farm App",
                        "Olá Maria Silva")));

        assertEquals("Unable to send email", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidEmailMessageBeforeSending() {
        EmailProperties emailProperties = buildEmailProperties();
        SmtpEmailSender sender = new SmtpEmailSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new AssertionError("transport should not be called");
                });

        IllegalArgumentException nullMessageException = assertThrows(IllegalArgumentException.class, () -> sender.send(null));
        assertEquals("emailMessage must not be null", nullMessageException.getMessage());

        IllegalArgumentException blankRecipientException = assertThrows(
                IllegalArgumentException.class,
                () -> sender.send(new EmailMessage(" ", "Confirme sua conta", "Olá Maria")));
        assertEquals("recipientEmail must not be blank", blankRecipientException.getMessage());

        IllegalArgumentException blankSubjectException = assertThrows(
                IllegalArgumentException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", " ", "Olá Maria")));
        assertEquals("subject must not be blank", blankSubjectException.getMessage());

        IllegalArgumentException blankBodyException = assertThrows(
                IllegalArgumentException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", " ")));
        assertEquals("body must not be blank", blankBodyException.getMessage());
    }

    @Test
    void shouldRequireConfiguredFromAddressWhenSending() {
        EmailProperties emailProperties = buildEmailProperties();
        emailProperties.setFrom("   ");
        SmtpEmailSender sender = new SmtpEmailSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new AssertionError("transport should not be called");
                });

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", "Olá Maria")));

        assertEquals("app.email.from must be configured when app.email.enabled is true", exception.getMessage());
    }

    private String decodeBody(String rawMessage) {
        String[] parts = rawMessage.split("\r\n\r\n", 2);
        return new String(Base64.getMimeDecoder().decode(parts[1]), StandardCharsets.UTF_8);
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
