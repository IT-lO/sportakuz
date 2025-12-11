package com.icio.sportakuz.config;

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
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
        var emf = new LocalContainerEntityManagerFactoryBean();
        // if creating DB for the first time switch to true, after creation switch to false
        var shouldOverwriteDatabase = true;

        emf.setDataSource(ds);
        emf.setPackagesToScan("com.icio");

        var vendor = new HibernateJpaVendorAdapter();
        vendor.setShowSql(false);

        vendor.setGenerateDdl(true);

        emf.setJpaVendorAdapter(vendor);

        var jpa = new HashMap<String, Object>();
        if(shouldOverwriteDatabase){
            jpa.put("hibernate.hbm2ddl.auto", "create");
        }
        else{
            jpa.put("hibernate.hbm2ddl.auto", "update");
        }

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
