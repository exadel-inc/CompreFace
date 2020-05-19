package com.exadel.frs.core.trainservice.config.repository;

import java.util.HashMap;
import javax.sql.DataSource;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages = "com.exadel.frs.core.trainservice.repository.postgres",
        entityManagerFactoryRef = "emPg",
        transactionManagerRef = "tmPg")
@EnableMongoRepositories(basePackages = "com.exadel.frs.core.trainservice.repository.mongo")
public class DbConfig {

    @Autowired
    private Environment env;

    @Bean("emPg")
    public LocalContainerEntityManagerFactoryBean pgEntityManager(@Qualifier("dsPg") DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.exadel.frs.core.trainservice.entity.postgres");
        val vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        val properties = new HashMap<String, Object>();
        properties.put("hibernate.ddl-auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "dsPg")
    public DataSource pgDataSource(@Autowired DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource-pg")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "tmPg")
    public PlatformTransactionManager pgTransactionManager(@Qualifier("emPg") LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        val transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean.getObject());
        return transactionManager;
    }
}
