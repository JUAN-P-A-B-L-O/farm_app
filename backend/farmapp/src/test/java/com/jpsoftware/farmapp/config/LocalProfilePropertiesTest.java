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
        assertThat(localProperties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create");
    }

    @Test
    void localProfileOverridesJpaDialectForH2() throws IOException {
        Properties defaultProperties = loadMainResourceProperties("application.properties");
        Properties localProperties = loadMainResourceProperties("application-local.properties");

        String effectiveDatabasePlatform = localProperties.getProperty(
                "spring.jpa.database-platform",
                defaultProperties.getProperty("spring.jpa.database-platform"));

        assertThat(effectiveDatabasePlatform).isEqualTo("org.hibernate.dialect.H2Dialect");
    }

    private Properties loadMainResourceProperties(String fileName) throws IOException {
        Path propertiesPath = Path.of("src/main/resources", fileName);
        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        }

        return properties;
    }
}
