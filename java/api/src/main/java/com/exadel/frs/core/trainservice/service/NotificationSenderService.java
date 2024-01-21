package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dto.CacheActionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.jdbc.PGDataSource;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Statement;


@Service("notificationSenderService")
@Slf4j
@RequiredArgsConstructor
public class NotificationSenderService {
    @Qualifier("dsPgNot")
    private final PGDataSource pgNotificationDatasource;
    private final ObjectMapper objectMapper;
    private PGConnection connection;

    @PostConstruct
    public void setUp() {
        try {
            this.connection = (PGConnection) pgNotificationDatasource.getConnection();
        } catch (SQLException e) {
            log.error("Error during connection to Postgres", e);
        }
    }

    public <T> void notifyCacheChange(CacheActionDto<T> cacheActionDto) {
        try {
            Statement statement = this.connection.createStatement();

            try {
                String actionString = String.format("NOTIFY face_collection_update_msg, '%s'",
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cacheActionDto));
                statement.execute(actionString);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }

            statement.close();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
