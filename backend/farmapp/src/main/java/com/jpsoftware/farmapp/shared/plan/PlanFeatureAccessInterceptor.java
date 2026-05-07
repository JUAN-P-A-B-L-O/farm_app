package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PlanFeatureAccessInterceptor implements HandlerInterceptor {

    private final AuthenticationContextService authenticationContextService;
    private final PlanAccessPolicy planAccessPolicy;

    public PlanFeatureAccessInterceptor(
            AuthenticationContextService authenticationContextService,
            PlanAccessPolicy planAccessPolicy) {
        this.authenticationContextService = authenticationContextService;
        this.planAccessPolicy = planAccessPolicy;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresPlanFeature requiresPlanFeature = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(),
                RequiresPlanFeature.class);
        if (requiresPlanFeature == null) {
            requiresPlanFeature = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(),
                    RequiresPlanFeature.class);
        }

        if (requiresPlanFeature == null) {
            return true;
        }

        UserPlan userPlan = authenticationContextService.getAuthenticatedUser()
                .map(authenticatedUser -> authenticatedUser.plan() == null
                        ? UserPlan.defaultPlan()
                        : authenticatedUser.plan())
                .orElse(UserPlan.defaultPlan());

        planAccessPolicy.assertHasAccess(userPlan, requiresPlanFeature.value());
        return true;
    }
}
