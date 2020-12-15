package com.exadel.frs.core.trainservice.config.repository;

import java.sql.DriverManager;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    @Value("${spring.datasource-pg.username}")
    private String dbUsername;

    @Value("${spring.datasource-pg.password}")
    private String dbPassword;

    @Value("${spring.datasource-pg.url}")
    private String dbUrl;

    @Bean
    public Listener dbListenerRun() throws SQLException {
        return new Listener(DriverManager.getConnection(dbUrl, dbUsername, dbPassword));
    }

    @Bean
    public Notifier dbNotifier() throws SQLException {
        return new Notifier(DriverManager.getConnection(dbUrl, dbUsername, dbPassword));
    }
}
