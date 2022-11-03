package com.exadel.frs.commonservice.system.liquibase.customchange;

import static com.exadel.frs.commonservice.system.global.RegExConstants.PROHIBITED_SPECIAL_CHARACTERS;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.repackaged.org.apache.commons.text.StringSubstitutor;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Getter
@Setter
public class RemoveSpecialCharactersCustomChange implements CustomTaskChange {

    private static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile(PROHIBITED_SPECIAL_CHARACTERS);

    private static final String COUNT_SQL_TEMPLATE = "SELECT COUNT(*) FROM ${table}";
    private static final String SELECT_SQL_TEMPLATE = "SELECT ${primaryKey}, ${target} FROM ${table}";
    private static final String UPDATE_SQL_TEMPLATE = "UPDATE ${table} SET ${target} = ? WHERE ${primaryKey} = ?";

    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";

    private String table;
    private String primaryKeyColumn;
    private String targetColumn;

    @Override
    public void execute(final Database database) throws CustomChangeException {
        try {
            JdbcConnection connection = (JdbcConnection) database.getConnection();
            List<Target> targets = getAllTargets(connection);
            List<Target> cleanedTargets = cleanTargets(targets);
            updateTargets(targets, cleanedTargets, connection);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomChangeException(e);
        }
    }

    private List<Target> getAllTargets(final JdbcConnection connection) throws DatabaseException, SQLException {
        String sql = StringSubstitutor.replace(SELECT_SQL_TEMPLATE, getParams(), PREFIX, SUFFIX);
        try (Statement statement = connection.createStatement()) {
            int rowCount = getRowCount(connection);
            List<Target> targets = new ArrayList<>(rowCount);
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Object primaryKey = resultSet.getObject(1);
                String value = resultSet.getString(2).trim();
                Target target = new Target(primaryKey, value);
                targets.add(target);
            }

            return targets;
        }
    }

    private int getRowCount(final JdbcConnection connection) throws DatabaseException, SQLException {
        String sql = StringSubstitutor.replace(COUNT_SQL_TEMPLATE, getParams(), PREFIX, SUFFIX);
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private List<Target> cleanTargets(final List<Target> targets) {
        List<Target> cleanedTargets = new ArrayList<>();
        for (Target target : targets) {
            Matcher matcher = SPECIAL_CHARACTERS_PATTERN.matcher(target.getValue());
            if (matcher.find()) {
                Target cleanedTarget = new Target(
                        target.getPrimaryKey(),
                        matcher.replaceAll("")
                );
                cleanedTargets.add(cleanedTarget);
            }
        }
        return cleanedTargets;
    }

    private void updateTargets(final List<Target> targets,
                               final List<Target> cleanedTargets,
                               final JdbcConnection connection) throws DatabaseException, SQLException {
        String sql = StringSubstitutor.replace(UPDATE_SQL_TEMPLATE, getParams(), PREFIX, SUFFIX);
        List<String> targetValues = targets.stream().map(Target::getValue).collect(Collectors.toList());

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Target cleanedTarget : cleanedTargets) {
                Object primaryKey = cleanedTarget.getPrimaryKey();
                String value = validateTargetValue(cleanedTarget.getValue(), targetValues)
                        ? cleanedTarget.getValue()
                        : UUID.randomUUID().toString();

                statement.setString(1, value);
                statement.setObject(2, primaryKey);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private boolean validateTargetValue(final String value, final List<String> values) {
        return StringUtils.isNotBlank(value) && !values.contains(value);
    }

    private Map<String, String> getParams() {
        return Map.of(
                "primaryKey", primaryKeyColumn,
                "target", targetColumn,
                "table", table
        );
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

    @Value
    private static class Target {

        Object primaryKey;
        String value;
    }
}
