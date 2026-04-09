package com.jpsoftware.farmapp.auth.service;

import com.jpsoftware.farmapp.auth.model.AuthenticatedUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthenticationContextService {

    public Optional<AuthenticatedUser> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser);
        }
        return Optional.empty();
    }

    public Optional<UUID> getAuthenticatedUserId() {
        return getAuthenticatedUser().map(AuthenticatedUser::id);
    }

    public String resolveUserId(String fallbackUserId) {
        return getAuthenticatedUserId()
                .map(UUID::toString)
                .orElseGet(() -> StringUtils.hasText(fallbackUserId) ? fallbackUserId : null);
    }
}
