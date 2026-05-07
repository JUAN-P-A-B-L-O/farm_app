package com.jpsoftware.farmapp.shared.config;

import com.jpsoftware.farmapp.shared.plan.PlanFeatureAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PlanAccessConfig implements WebMvcConfigurer {

    private final PlanFeatureAccessInterceptor planFeatureAccessInterceptor;

    public PlanAccessConfig(PlanFeatureAccessInterceptor planFeatureAccessInterceptor) {
        this.planFeatureAccessInterceptor = planFeatureAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(planFeatureAccessInterceptor);
    }
}
