package com.exadel.frs.core.trainservice.config.repository;

import static com.exadel.frs.core.trainservice.system.global.Constants.SERVER_UUID;
import com.exadel.frs.core.trainservice.dto.DbActionDto;
import com.exadel.frs.core.trainservice.service.DbActionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
class Listener {

    @Autowired
    private DbActionService actionService;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Connection conn;
    private final PGConnection pgconn;

    Listener(Connection conn) throws SQLException {
        this.conn = conn;
        this.pgconn = (PGConnection)conn;
        Statement stmt = conn.createStatement();
        stmt.execute("LISTEN updatemsg");
        stmt.close();
        log.info(String.format("Listener %s is started", SERVER_UUID));
    }

    @Scheduled(fixedRate=500)
    public void listen() {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                rs.close();
                stmt.close();

                org.postgresql.PGNotification[] notifications = pgconn.getNotifications();
                if (notifications != null) {
                    for (final org.postgresql.PGNotification notification : notifications) {
                        log.info("Get notification: " + notification.getName());
                        actionService.synchronizeCache(mapper.readValue(notification.getParameter(), DbActionDto.class));
                    }
                }
            } catch (SQLException | JsonProcessingException e) {
                e.printStackTrace();
            }
    }

}
