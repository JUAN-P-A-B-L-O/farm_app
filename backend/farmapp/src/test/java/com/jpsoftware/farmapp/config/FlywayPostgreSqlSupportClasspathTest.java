package com.jpsoftware.farmapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension;
import org.flywaydb.database.postgresql.PostgreSQLDatabaseType;
import org.junit.jupiter.api.Test;

class FlywayPostgreSqlSupportClasspathTest {

    @Test
    void postgresqlFlywayModuleIsAvailableOnClasspath() {
        PostgreSQLDatabaseType databaseType = new PostgreSQLDatabaseType();
        PostgreSQLConfigurationExtension configurationExtension = new PostgreSQLConfigurationExtension();

        assertThat(databaseType.getName()).isEqualTo("PostgreSQL");
        assertThat(databaseType.handlesJDBCUrl("jdbc:postgresql://localhost:5432/farmdb")).isTrue();
        assertThat(configurationExtension.getNamespace()).isEqualTo("postgresql");
    }
}
