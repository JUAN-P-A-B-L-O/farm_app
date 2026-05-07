package com.jpsoftware.farmapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class LocalProfilePropertiesTest {

    @Test
    void localProfileUsesInMemoryH2DatasourceSettings() throws IOException {
        Properties localProperties = loadMainResourceProperties("application-local.properties");

        assertThat(localProperties.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:h2:mem:farmdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        assertThat(localProperties.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.h2.Driver");
        assertThat(localProperties.getProperty("spring.datasource.username")).isEqualTo("sa");
        assertThat(localProperties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }

    @Test
    void localProfileLeavesJpaDialectUnsetForAutoDetection() throws IOException {
        Properties localProperties = loadMainResourceProperties("application-local.properties");
        assertThat(localProperties.getProperty("spring.jpa.database-platform")).isNull();
    }

    @Test
    void baseApplicationPropertiesEnableFlywayAndHibernateValidation() throws IOException {
        Properties applicationProperties = loadMainResourceProperties("application.properties");

        assertThat(applicationProperties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(applicationProperties.getProperty("spring.flyway.enabled")).isEqualTo("true");
        assertThat(applicationProperties.getProperty("spring.flyway.locations"))
                .isEqualTo("classpath:db/migration");
        assertThat(applicationProperties.getProperty("spring.flyway.baseline-on-migrate")).isEqualTo("true");
        assertThat(applicationProperties.getProperty("spring.flyway.baseline-version")).isEqualTo("1");
    }

    @Test
    void testApplicationPropertiesUseValidatedH2Schema() throws IOException {
        Properties testProperties = loadTestResourceProperties("application.properties");

        assertThat(testProperties.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:h2:mem:farmdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        assertThat(testProperties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }

    private Properties loadMainResourceProperties(String fileName) throws IOException {
        return loadProperties(Path.of("src/main/resources", fileName));
    }

    private Properties loadTestResourceProperties(String fileName) throws IOException {
        return loadProperties(Path.of("src/test/resources", fileName));
    }

    private Properties loadProperties(Path propertiesPath) throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        }

        return properties;
    }
}
