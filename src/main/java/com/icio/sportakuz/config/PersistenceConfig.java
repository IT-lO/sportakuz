package com.icio.sportakuz.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jndi.JndiTemplate;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.naming.NamingException;
import java.util.HashMap;

/**
 * Konfiguracja  JPA + Hibernate dla aplikacji.
 * Źródło danych pobierane przez JNDI (serwer aplikacyjny), migracje schematu wykonuje Flyway,
 * a walidacja poprawności mapowania odbywa się przez hbm2ddl.validate.
 */
@Configuration
@EnableTransactionManagement
// tu dopisujemy kolejne reposy
@EnableJpaRepositories(basePackages = {
        "com.icio.sportakuz.repo"
})
public class PersistenceConfig {

    /** Pobiera DataSource z kontekstu JNDI. */
    @Bean
    public DataSource dataSource() throws NamingException {
        return (DataSource) new JndiTemplate().lookup("java:comp/env/jdbc/sportakuz");
    }

    /** Buduje fabrykę EntityManager z adapterem Hibernate i podstawowymi właściwościami. */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds, Flyway flyway) {
        flyway.migrate();

        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan("com.icio.sportakuz");

        var vendor = new HibernateJpaVendorAdapter();
        vendor.setShowSql(false);
        vendor.setGenerateDdl(false); // schemat robi Flyway
        emf.setJpaVendorAdapter(vendor);

        var jpa = new HashMap<String, Object>();
        jpa.put("hibernate.hbm2ddl.auto", "validate");
        jpa.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpa.put("hibernate.jdbc.time_zone", "UTC");
        emf.setJpaPropertyMap(jpa);

        return emf;
    }

    /** Manager transakcji JPA oparty o fabrykę encji. */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
