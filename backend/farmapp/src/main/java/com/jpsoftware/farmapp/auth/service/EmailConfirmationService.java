package com.jpsoftware.farmapp.auth.service;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EmailConfirmationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfirmationService.class);

    private final UserRepository userRepository;
    private final EmailConfirmationTokenService tokenService;
    private final EmailSender emailSender;
    private final String frontendBaseUrl;
    private final String confirmationSubject;
    private final long tokenExpirationHours;

    public EmailConfirmationService(
            UserRepository userRepository,
            EmailConfirmationTokenService tokenService,
            EmailSender emailSender,
            @Value("${app.auth.email-confirmation.frontend-base-url}") String frontendBaseUrl,
            @Value("${app.email.confirmation.subject}") String confirmationSubject,
            @Value("${app.auth.email-confirmation.token-expiration-hours}") long tokenExpirationHours) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.emailSender = emailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.confirmationSubject = confirmationSubject;
        this.tokenExpirationHours = tokenExpirationHours;
    }

    @Transactional
    public void initializePendingConfirmation(UserEntity userEntity) {
        issueConfirmationToken(userEntity, true);
    }

    @Transactional
    public void resendConfirmation(String email) {
        UserEntity userEntity = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userEntity.isEmailConfirmed()) {
            throw new ConflictException("Email already confirmed");
        }

        issueConfirmationToken(userEntity, false);
    }

    @Transactional
    public void confirmEmail(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ValidationException("token must not be blank");
        }

        String tokenHash = tokenService.hashToken(token.trim());
        UserEntity userEntity = userRepository.findByEmailConfirmationTokenHash(tokenHash)
                .orElseThrow(() -> new ValidationException("Invalid or expired email confirmation token"));

        if (userEntity.isEmailConfirmed()) {
            throw new ConflictException("Email already confirmed");
        }
        if (userEntity.getEmailConfirmationTokenExpiresAt() == null
                || userEntity.getEmailConfirmationTokenExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Invalid or expired email confirmation token");
        }

        userEntity.setEmailConfirmed(true);
        userEntity.setEmailConfirmationTokenHash(null);
        userEntity.setEmailConfirmationTokenExpiresAt(null);
        userRepository.save(userEntity);
    }

    private void issueConfirmationToken(UserEntity userEntity, boolean suppressEmailFailures) {
        String rawToken = tokenService.generateToken();
        userEntity.setEmailConfirmed(false);
        userEntity.setEmailConfirmationTokenHash(tokenService.hashToken(rawToken));
        userEntity.setEmailConfirmationTokenExpiresAt(Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS));
        userRepository.save(userEntity);

        EmailMessage emailMessage = new EmailMessage(
                userEntity.getEmail(),
                confirmationSubject,
                buildConfirmationBody(userEntity.getName(), buildConfirmationUrl(rawToken)));
        try {
            emailSender.send(emailMessage);
        } catch (RuntimeException exception) {
            if (!suppressEmailFailures) {
                throw exception;
            }
            logger.warn(
                    "Failed to send confirmation email after user registration for userId={}",
                    userEntity.getId(),
                    exception);
        }
    }

    private String buildConfirmationUrl(String rawToken) {
        return frontendBaseUrl.replaceAll("/+$", "") + "/confirm-email?token=" + rawToken;
    }

    private String buildConfirmationBody(String recipientName, String confirmationUrl) {
        String resolvedName = StringUtils.hasText(recipientName) ? recipientName.trim() : "usuário";
        return """
                Olá %s,

                Recebemos o cadastro da sua conta no Farm App.
                Confirme seu e-mail acessando o link abaixo:
                %s

                Se você não solicitou este cadastro, ignore esta mensagem.
                """.formatted(resolvedName, confirmationUrl);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
