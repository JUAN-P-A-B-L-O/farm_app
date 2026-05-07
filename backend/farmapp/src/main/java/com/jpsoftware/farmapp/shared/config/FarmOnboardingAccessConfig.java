package com.jpsoftware.farmapp.shared.config;

import com.jpsoftware.farmapp.shared.onboarding.FarmOnboardingAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FarmOnboardingAccessConfig implements WebMvcConfigurer {

    private final FarmOnboardingAccessInterceptor farmOnboardingAccessInterceptor;

    public FarmOnboardingAccessConfig(FarmOnboardingAccessInterceptor farmOnboardingAccessInterceptor) {
        this.farmOnboardingAccessInterceptor = farmOnboardingAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(farmOnboardingAccessInterceptor).order(-100);
    }
}
