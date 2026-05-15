package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final EmailProperties emailProperties;
    private final JavaMailSender mailSender;

    public SmtpEmailSender(EmailProperties emailProperties, JavaMailSender mailSender) {
        this.emailProperties = emailProperties;
        this.mailSender = mailSender;
    }

    @Override
    public void send(EmailMessage emailMessage) {
        validateEmailMessage(emailMessage);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(resolveFromAddress());
            helper.setTo(emailMessage.recipientEmail().trim());
            helper.setSubject(emailMessage.subject().trim());
            helper.setText(emailMessage.body(), false);
            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException exception) {
            throw new EmailDispatchException("Unable to send email", exception);
        }
    }

    private void validateEmailMessage(EmailMessage emailMessage) {
        if (emailMessage == null) {
            throw new IllegalArgumentException("emailMessage must not be null");
        }
        if (!StringUtils.hasText(emailMessage.recipientEmail())) {
            throw new IllegalArgumentException("recipientEmail must not be blank");
        }
        if (!StringUtils.hasText(emailMessage.subject())) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (!StringUtils.hasText(emailMessage.body())) {
            throw new IllegalArgumentException("body must not be blank");
        }
    }

    private String resolveFromAddress() {
        if (!StringUtils.hasText(emailProperties.getFrom())) {
            throw new IllegalStateException("app.email.from must be configured when app.email.enabled is true");
        }
        return emailProperties.getFrom().trim();
    }
}
