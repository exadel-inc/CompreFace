package com.exadel.frs.core.trainservice.config.repository;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class NotificationDbConfig {

    @Autowired
    private Environment env;

//    @Bean(name = "dsPgNot")
//    public PGDataSource pgNotificationDatasource() {
//        PGDataSource dataSource = new PGDataSource();
//
//        String dbUrl = env.getProperty("spring.datasource-pg.url");
//        String dbUsername= env.getProperty("spring.datasource-pg.username");
//        String dbPassword= env.getProperty("spring.datasource-pg.password");
//
//        String databaseUrl = dbUrl.replaceAll("postgresql", "pgsql");
//
//        dataSource.setDatabaseUrl(databaseUrl);
//        dataSource.setUser(dbUsername);
//        dataSource.setPassword(dbPassword);
//
//        return dataSource;
//    }

    @Bean
    public PGConnection pgConnection() throws SQLException {
        String dbUrl = env.getProperty("spring.datasource-pg.url");
        String dbUsername= env.getProperty("spring.datasource-pg.username");
        String dbPassword= env.getProperty("spring.datasource-pg.password");

        String databaseUrl = dbUrl.replaceAll("postgresql", "pgsql");
        return DriverManager.getConnection(databaseUrl, dbUsername, dbPassword).unwrap(PGConnection.class);
    }


}
