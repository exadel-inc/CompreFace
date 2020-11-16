package com.exadel.frs.core.trainservice.config.repository;

import com.exadel.frs.core.trainservice.dto.DbActionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Notifier{

    private final Connection conn;
    private final ObjectMapper mapper = new ObjectMapper();

    public Notifier(Connection conn) {
        this.conn = conn;
    }

    public void notifyWithMessage(DbActionDto actionDto) {
            try {
                String actionString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actionDto);
                Statement stmt = conn.createStatement();
                stmt.execute(String.format("SELECT pg_notify('face_collection_update_msg', '%s');", actionString));
                stmt.close();
            } catch (SQLException | JsonProcessingException sqle) {
                sqle.printStackTrace();
            }
    }

}
