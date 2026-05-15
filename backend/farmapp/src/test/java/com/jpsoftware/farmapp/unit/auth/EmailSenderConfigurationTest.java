package com.jpsoftware.farmapp.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.jpsoftware.farmapp.auth.infrastructure.EmailConfiguration;
import com.jpsoftware.farmapp.auth.infrastructure.LoggingEmailSender;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

class EmailSenderConfigurationTest {

    private final ApplicationContextRunner autoConfiguredContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
            .withUserConfiguration(EmailConfiguration.class, LoggingEmailSender.class, SmtpEmailSender.class);

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

    @Test
    void shouldAutoConfigureJavaMailSenderWhenEmailIsEnabled() {
        autoConfiguredContextRunner
                .withPropertyValues(
                        "app.email.enabled=true",
                        "app.email.from=no-reply@farmapp.local",
                        "spring.mail.host=localhost")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
                });
    }

    private ConfigurableApplicationContext runContext(String... args) {
        return new SpringApplicationBuilder(EmailSenderTestApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @SpringBootConfiguration
    @Import({EmailConfiguration.class, LoggingEmailSender.class, SmtpEmailSender.class})
    static class EmailSenderTestApplication {

        @Bean
        JavaMailSender javaMailSender() {
            return org.mockito.Mockito.mock(JavaMailSender.class);
        }
    }
}
