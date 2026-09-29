package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfiguration {

    @Bean
    EmailSender emailSender(
            EmailProperties emailProperties,
            MailProperties mailProperties,
            ObjectProvider<JavaMailSender> javaMailSenderProvider) {
        if (shouldUseSmtp(emailProperties, mailProperties)) {
            JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
            if (javaMailSender == null) {
                return new LoggingEmailSender();
            }
            return new SmtpEmailSender(emailProperties, javaMailSender);
        }
        return new LoggingEmailSender();
    }

    private boolean shouldUseSmtp(EmailProperties emailProperties, MailProperties mailProperties) {
        if (emailProperties.getEnabled() != null) {
            return emailProperties.getEnabled();
        }

        if (!StringUtils.hasText(emailProperties.getFrom())) {
            return false;
        }
        if (!StringUtils.hasText(mailProperties.getHost())) {
            return false;
        }

        String smtpAuthProperty = mailProperties.getProperties().get("mail.smtp.auth");
        boolean smtpAuthEnabled = Boolean.parseBoolean(smtpAuthProperty);
        if (!smtpAuthEnabled) {
            return true;
        }

        return StringUtils.hasText(mailProperties.getUsername())
                && StringUtils.hasText(mailProperties.getPassword());
    }
}
