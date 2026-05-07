package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.auth.service.EmailConfirmationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(EmailConfirmationSender.class)
public class SimpleEmailConfirmationSender implements EmailConfirmationSender {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEmailConfirmationSender.class);

    public SimpleEmailConfirmationSender() {
    }

    @Override
    public void sendConfirmationEmail(String recipientEmail, String recipientName, String confirmationUrl) {
        logger.warn(
                "SMTP email delivery is disabled. Confirmation link for {} ({}): {}",
                recipientName,
                recipientEmail,
                confirmationUrl);
    }
}
