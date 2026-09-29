package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.auth.infrastructure.EmailProperties;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpEmailSenderTest {

    @Test
    void shouldSendTransactionalEmailWithConfiguredMetadata() throws Exception {
        EmailProperties emailProperties = buildEmailProperties();
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, mailSender);

        sender.send(new EmailMessage(
                "  maria@farm.com  ",
                "Confirme sua conta no Farm App",
                "Olá Maria Silva,\n\nUse o link para confirmar."));

        verify(mailSender).send(mimeMessage);
        assertEquals("no-reply@farmapp.local", ((InternetAddress) mimeMessage.getFrom()[0]).getAddress());
        assertEquals("maria@farm.com", ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress());
        assertEquals("Confirme sua conta no Farm App", mimeMessage.getSubject());
        assertTrue(mimeMessage.getContent().toString().contains("Olá Maria Silva,"));
    }

    @Test
    void shouldWrapMailFailuresAsEmailDispatchException() {
        EmailProperties emailProperties = buildEmailProperties();
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("smtp error")).when(mailSender).send(mimeMessage);
        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, mailSender);

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
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, mailSender);

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
        JavaMailSender mailSender = org.mockito.Mockito.mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, mailSender);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", "Olá Maria")));

        assertEquals("app.email.from must be configured when SMTP email delivery is enabled", exception.getMessage());
    }

    private EmailProperties buildEmailProperties() {
        EmailProperties emailProperties = new EmailProperties();
        emailProperties.setFrom("no-reply@farmapp.local");
        emailProperties.getConfirmation().setSubject("Confirme sua conta no Farm App");
        emailProperties.setEnabled(Boolean.TRUE);
        return emailProperties;
    }
}
