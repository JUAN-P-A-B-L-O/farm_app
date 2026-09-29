package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(EmailMessage emailMessage) {
        logger.warn(
                "SMTP email delivery is disabled. Transactional email to {} with subject '{}' was not sent.",
                emailMessage.recipientEmail(),
                emailMessage.subject());
    }
}
