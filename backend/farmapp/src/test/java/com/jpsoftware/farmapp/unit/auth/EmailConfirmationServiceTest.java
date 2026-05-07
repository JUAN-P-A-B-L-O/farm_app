package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.auth.service.EmailConfirmationService;
import com.jpsoftware.farmapp.auth.service.EmailConfirmationTokenService;
import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EmailConfirmationServiceTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final EmailConfirmationTokenService tokenService =
            org.mockito.Mockito.mock(EmailConfirmationTokenService.class);
    private final EmailSender emailSender = org.mockito.Mockito.mock(EmailSender.class);

    @Test
    void shouldComposeConfirmationEmailWithConfiguredSubjectAndTrimmedFrontendBaseUrl() {
        EmailConfirmationService service = new EmailConfirmationService(
                userRepository,
                tokenService,
                emailSender,
                "http://localhost:5173///",
                "Confirme sua conta no Farm App",
                24);
        UserEntity user = new UserEntity();
        user.setName("   ");
        user.setEmail("maria@farm.com");
        Instant beforeIssuance = Instant.now();

        when(tokenService.generateToken()).thenReturn("raw-token");
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(userRepository.save(user)).thenReturn(user);

        service.initializePendingConfirmation(user);

        assertFalse(user.isEmailConfirmed());
        assertEquals("hashed-token", user.getEmailConfirmationTokenHash());
        assertNotNull(user.getEmailConfirmationTokenExpiresAt());
        assertTrue(user.getEmailConfirmationTokenExpiresAt().isAfter(beforeIssuance));

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(emailCaptor.capture());
        EmailMessage sentEmail = emailCaptor.getValue();
        assertEquals("maria@farm.com", sentEmail.recipientEmail());
        assertEquals("Confirme sua conta no Farm App", sentEmail.subject());
        assertTrue(sentEmail.body().contains("Olá usuário,"));
        assertTrue(sentEmail.body().contains("http://localhost:5173/login?mode=confirm&token=raw-token"));
    }
}
