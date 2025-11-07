package com.icio.sportakuz.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Konfiguracja Flyway ograniczona do wskazania lokalizacji migracji.
 * Pozostawia walidację i nie wykonuje baseline na nowej bazie.
 * Jeśli baza nie jest pusta i brak historii, ustaw chwilowo baselineOnMigrate(true).
 */

@Configuration
public class FlywayConfig {

    // Flyway – uruchomi migracje z classpath:db/migration korzystając z głównego DataSource
    @Bean
    public Flyway flyway(DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")   // src/main/resources/db/migration
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}
