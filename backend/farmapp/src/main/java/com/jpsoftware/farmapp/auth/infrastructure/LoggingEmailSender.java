package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(EmailSender.class)
public class LoggingEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(EmailMessage emailMessage) {
        logger.warn(
                "SMTP email delivery is disabled. Transactional email to {} with subject '{}': {}",
                emailMessage.recipientEmail(),
                emailMessage.subject(),
                emailMessage.body());
    }
}
