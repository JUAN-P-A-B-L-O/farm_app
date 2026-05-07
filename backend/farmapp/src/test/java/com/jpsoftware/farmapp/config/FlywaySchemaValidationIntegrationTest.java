package com.jpsoftware.farmapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FlywaySchemaValidationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayBuildsSchemaThatSupportsJpaValidationOnH2() {
        assertThat(Arrays.stream(flyway.info().applied())
                .map(info -> info.getVersion().getVersion()))
                .contains("1", "2");

        assertTableExists("USERS");
        assertTableExists("MILK_PRICES");
        assertTableExists("USER_FARM_ASSIGNMENTS");
        assertColumnExists("USERS", "EMAIL_CONFIRMED");
        assertColumnExists("USERS", "EMAIL_CONFIRMATION_TOKEN_HASH");
        assertColumnExists("USERS", "EMAIL_CONFIRMATION_TOKEN_EXPIRES_AT");
        assertColumnExists("USERS", "AVATAR_URL");
        assertColumnExists("USERS", "PLAN");
    }

    private void assertTableExists(String tableName) {
        Integer matches = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND TABLE_NAME = ?
                """,
                Integer.class,
                tableName);

        assertThat(matches).isEqualTo(1);
    }

    private void assertColumnExists(String tableName, String columnName) {
        Integer matches = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'PUBLIC'
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                tableName,
                columnName);

        assertThat(matches).isEqualTo(1);
    }
}
