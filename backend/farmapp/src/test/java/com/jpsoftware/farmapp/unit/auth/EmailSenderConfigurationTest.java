package com.jpsoftware.farmapp.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.jpsoftware.farmapp.auth.infrastructure.EmailConfiguration;
import com.jpsoftware.farmapp.auth.infrastructure.LoggingEmailSender;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

class EmailSenderConfigurationTest {

    @Test
    void shouldUseLoggingEmailSenderWhenEmailIsDisabled() {
        try (ConfigurableApplicationContext context = runContext("--app.email.enabled=false")) {
            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
            assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
            assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
        }
    }

    @Test
    void shouldUseLoggingEmailSenderWhenEmailPropertyIsMissing() {
        try (ConfigurableApplicationContext context = runContext()) {
            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
            assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
            assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
        }
    }

    @Test
    void shouldUseSmtpEmailSenderWhenEmailIsEnabled() {
        try (ConfigurableApplicationContext context = runContext("--app.email.enabled=true")) {
            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
            assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
            assertThat(context.getBeansOfType(LoggingEmailSender.class)).isEmpty();
        }
    }

    private ConfigurableApplicationContext runContext(String... args) {
        return new SpringApplicationBuilder(EmailSenderTestApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @SpringBootConfiguration
    @Import({EmailConfiguration.class, LoggingEmailSender.class, SmtpEmailSender.class})
    static class EmailSenderTestApplication {
    }
}
