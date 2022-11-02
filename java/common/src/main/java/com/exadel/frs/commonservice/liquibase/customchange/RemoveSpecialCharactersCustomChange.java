package com.exadel.frs.commonservice.liquibase.customchange;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.regex.Pattern;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class RemoveSpecialCharactersCustomChange implements CustomTaskChange {

    // there is a double '%' to escape this character for the String.format method
    private static final Pattern PATTERN = Pattern.compile("[`~!@\"#№$%%^:;&?<>(){|},/\\\\*+=]");

    private static final String SELECT_SQL_TEMPLATE = "SELECT %1$s, %2$s FROM %3$s WHERE %2$s ~ " + "'" + PATTERN.pattern() + "'";
    private static final String UPDATE_SQL_TEMPLATE = "UPDATE %1$s SET %2$s = ? WHERE %3$s = ?";

    private String table;
    private String primaryKeyColumn;
    private String targetColumn;

    @Override
    public void execute(final Database database) throws CustomChangeException {
        try {
            JdbcConnection connection = (JdbcConnection) database.getConnection();
            String selectSql = String.format(SELECT_SQL_TEMPLATE, primaryKeyColumn, targetColumn, table);
            String updateSql = String.format(UPDATE_SQL_TEMPLATE, table, targetColumn, primaryKeyColumn);
            try (
                    Statement selectStatement = connection.createStatement();
                    PreparedStatement updateStatement = connection.prepareStatement(updateSql)
            ) {
                ResultSet resultSet = selectStatement.executeQuery(selectSql);
                while (resultSet.next()) {
                    Object primaryKey = resultSet.getObject(primaryKeyColumn);
                    String target = resultSet.getString(targetColumn);
                    String cleanTarget = PATTERN.matcher(target.trim()).replaceAll("");
                    if (cleanTarget.isBlank()) {
                        // if the target column becomes a blank string, it will be assigned a random UUID
                        cleanTarget = UUID.randomUUID().toString();
                    }
                    updateStatement.setString(1, cleanTarget);
                    updateStatement.setObject(2, primaryKey);
                    updateStatement.addBatch();
                }
                updateStatement.executeBatch();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomChangeException(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(final ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(final Database database) {
        return null;
    }
}
