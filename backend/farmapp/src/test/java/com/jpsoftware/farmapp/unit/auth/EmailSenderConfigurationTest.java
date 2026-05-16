package com.jpsoftware.farmapp.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.jpsoftware.farmapp.auth.infrastructure.EmailConfiguration;
import com.jpsoftware.farmapp.auth.infrastructure.LoggingEmailSender;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

class EmailSenderConfigurationTest {

    private final ApplicationContextRunner autoConfiguredContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
            .withUserConfiguration(EmailConfiguration.class);
    private final ApplicationContextRunner missingMailSenderContextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EmailConfiguration.class, MailPropertiesOnlyTestConfiguration.class);

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
        try (ConfigurableApplicationContext context = runContext("--spring.mail.host=")) {
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
    void shouldUseSmtpEmailSenderWhenBrevoCredentialsAreConfigured() {
        autoConfiguredContextRunner
                .withPropertyValues(
                        "app.email.from=no-reply@farmapp.local",
                        "spring.mail.host=smtp-relay.brevo.com",
                        "spring.mail.username=brevo-user",
                        "spring.mail.password=brevo-pass",
                        "spring.mail.properties.mail.smtp.auth=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
                });
    }

    @Test
    void shouldUseSmtpEmailSenderWhenSmtpAuthIsDisabled() {
        autoConfiguredContextRunner
                .withPropertyValues(
                        "app.email.from=no-reply@farmapp.local",
                        "spring.mail.host=localhost",
                        "spring.mail.properties.mail.smtp.auth=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
                });
    }

    @Test
    void shouldKeepLoggingFallbackWhenSmtpAuthIsEnabledButCredentialsAreMissing() {
        autoConfiguredContextRunner
                .withPropertyValues(
                        "app.email.from=no-reply@farmapp.local",
                        "spring.mail.host=smtp-relay.brevo.com",
                        "spring.mail.properties.mail.smtp.auth=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
                });
    }

    @Test
    void shouldKeepLoggingFallbackWhenSmtpIsEnabledButJavaMailSenderIsUnavailable() {
        missingMailSenderContextRunner
                .withPropertyValues("app.email.enabled=true")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(JavaMailSender.class);
                    assertThat(context).hasSingleBean(EmailSender.class);
                    assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
                });
    }

    private ConfigurableApplicationContext runContext(String... args) {
        return new SpringApplicationBuilder(EmailSenderTestApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @SpringBootConfiguration
    @Import(EmailConfiguration.class)
    @EnableConfigurationProperties(MailProperties.class)
    static class EmailSenderTestApplication {

        @Bean
        JavaMailSender javaMailSender() {
            return org.mockito.Mockito.mock(JavaMailSender.class);
        }
    }

    @SpringBootConfiguration
    @EnableConfigurationProperties(MailProperties.class)
    static class MailPropertiesOnlyTestConfiguration {
    }
}
