package com.jpsoftware.farmapp.shared.onboarding;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.farm.service.FarmService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FarmOnboardingAccessInterceptor implements HandlerInterceptor {

    private static final String FARMS_PATH = "/farms";
    private static final String UPDATE_PASSWORD_PATH = "/users/me/password";
    private static final String REQUIRED_MESSAGE = "Create a farm before accessing this feature";

    private final AuthenticationContextService authenticationContextService;
    private final FarmService farmService;

    public FarmOnboardingAccessInterceptor(
            AuthenticationContextService authenticationContextService,
            FarmService farmService) {
        this.authenticationContextService = authenticationContextService;
        this.farmService = farmService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        if (isOnboardingAllowed(request)) {
            return true;
        }

        UUID authenticatedUserId = authenticationContextService.getAuthenticatedUserId().orElse(null);
        if (authenticatedUserId == null || farmService.hasAccessibleFarm(authenticatedUserId)) {
            return true;
        }

        throw new FarmOnboardingRequiredException(REQUIRED_MESSAGE);
    }

    private boolean isOnboardingAllowed(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        if (FARMS_PATH.equals(path) && ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method))) {
            return true;
        }

        return UPDATE_PASSWORD_PATH.equals(path) && "PUT".equalsIgnoreCase(method);
    }
}
