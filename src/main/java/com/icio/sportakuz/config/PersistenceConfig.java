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

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.icio.sportakuz.repository")
public class PersistenceConfig {

    @Bean
    public DataSource dataSource() throws NamingException {
        // JNDI z TomEE zgodnie z WEB-INF/resources.xml
        return (DataSource) new JndiTemplate().lookup("java:comp/env/jdbc/sportakuz");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan("com.icio.sportakuz.domain"); // encje + enumy

        var vendor = new HibernateJpaVendorAdapter();
        vendor.setShowSql(false);
        vendor.setGenerateDdl(false); // schemat robi Flyway
        emf.setJpaVendorAdapter(vendor);

        var jpa = new HashMap<String, Object>();
        jpa.put("hibernate.hbm2ddl.auto", "validate"); // sprawdza zgodność encji z DB
        jpa.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpa.put("hibernate.jdbc.time_zone", "UTC");
        emf.setJpaPropertyMap(jpa);

        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
