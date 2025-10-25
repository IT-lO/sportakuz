package com.icio.sportakuz.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    // DataSource z JNDI (z resources.xml: id="jdbc/sportakuz")
    @Bean
    public DataSource dataSource() throws NamingException {
        JndiObjectFactoryBean jndi = new JndiObjectFactoryBean();
        jndi.setJndiName("jdbc/sportakuz");           // <-- musi się zgadzać z WEB-INF/resources.xml
        jndi.setResourceRef(true);
        jndi.setProxyInterface(DataSource.class);
        jndi.afterPropertiesSet();
        return (DataSource) jndi.getObject();
    }

    // Flyway – uruchomi migracje z classpath:db/migration
    @Bean
    public Flyway flyway(DataSource ds) {
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")   // src/main/resources/db/migration
                .baselineOnMigrate(true)               // jeśli baza była pusta, OK; jeśli nie – też OK
                .load();
        flyway.migrate();                              // <— wykonuje V1__*.sql
        return flyway;
    }
}
