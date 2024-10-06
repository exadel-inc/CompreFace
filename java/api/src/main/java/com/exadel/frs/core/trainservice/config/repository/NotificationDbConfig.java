package com.exadel.frs.core.trainservice.config.repository;

import com.impossibl.postgres.jdbc.PGDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class NotificationDbConfig {

    @Bean(name = "dsPgNot")
    public PGDataSource pgNotificationDatasource(DataSourceProperties dataSourceProperties) {
        String pgsqlUrl = dataSourceProperties.getUrl().replace("postgresql", "pgsql");
        dataSourceProperties.setUrl(pgsqlUrl);

        PGDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(PGDataSource.class).build();
        dataSource.setHousekeeper(false);

        return dataSource;
    }
}
