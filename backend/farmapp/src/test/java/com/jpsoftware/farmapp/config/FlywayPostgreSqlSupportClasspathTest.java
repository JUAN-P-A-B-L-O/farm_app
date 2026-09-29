package com.jpsoftware.farmapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension;
import org.flywaydb.database.postgresql.PostgreSQLDatabaseType;
import org.junit.jupiter.api.Test;

class FlywayPostgreSqlSupportClasspathTest {

    @Test
    void postgresqlFlywayModuleIsAvailableOnClasspath() {
        ClassicConfiguration configuration = new ClassicConfiguration();
        DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForUrl(
                "jdbc:postgresql://localhost:5432/farmdb",
                configuration);
        PostgreSQLConfigurationExtension configurationExtension =
                configuration.getConfigurationExtension(PostgreSQLConfigurationExtension.class);

        assertThat(databaseType).isInstanceOf(PostgreSQLDatabaseType.class);
        assertThat(databaseType.getName()).isEqualTo("PostgreSQL");
        assertThat(databaseType.handlesJDBCUrl("jdbc:postgresql://localhost:5432/farmdb")).isTrue();
        assertThat(configurationExtension).isNotNull();
        assertThat(configurationExtension.getNamespace()).isEqualTo("postgresql");
    }
}
