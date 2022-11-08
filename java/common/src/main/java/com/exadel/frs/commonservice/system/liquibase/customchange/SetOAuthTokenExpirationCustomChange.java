package com.exadel.frs.commonservice.system.liquibase.customchange;

import static java.time.ZoneOffset.UTC;
import static java.util.Date.from;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class SetOAuthTokenExpirationCustomChange implements CustomTaskChange {

    private static final String REFRESH_TOKEN = "refresh_token";

    private static final String SET_ACCESS_TOKEN_EXPIRATION_SQL = "UPDATE oauth_access_token SET expiration = ? WHERE client_id = ?";
    private static final String SET_REFRESH_TOKEN_EXPIRATION_SQL = "UPDATE oauth_refresh_token SET expiration = ? WHERE token_id IN (SELECT refresh_token FROM oauth_access_token WHERE client_id = ?)";

    private String clientId;
    private String authorizedGrantTypes;
    private int accessTokenValidity;
    private int refreshTokenValidity;

    @Override
    public void execute(final Database database) throws CustomChangeException {
        try {
            JdbcConnection connection = (JdbcConnection) database.getConnection();
            setAccessTokenExpiration(connection);
            if (authorizedGrantTypes.contains(REFRESH_TOKEN)) {
                setRefreshTokenExpiration(connection);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomChangeException(e);
        }
    }

    private void setAccessTokenExpiration(final JdbcConnection connection)
            throws DatabaseException, SQLException {
        int updateCount = setTokenExpiration(
                connection,
                SET_ACCESS_TOKEN_EXPIRATION_SQL,
                accessTokenValidity
        );
        log.info(
                "Updated {} access tokens for client {}",
                updateCount,
                clientId
        );
    }

    private void setRefreshTokenExpiration(final JdbcConnection connection)
            throws DatabaseException, SQLException {
        int updateCount = setTokenExpiration(
                connection,
                SET_REFRESH_TOKEN_EXPIRATION_SQL,
                refreshTokenValidity
        );
        log.info(
                "Updated {} refresh tokens for client {}",
                updateCount,
                clientId
        );
    }

    private int setTokenExpiration(final JdbcConnection connection, final String sql, final int tokenValidity)
            throws DatabaseException, SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            Date expiration = getExpiration(tokenValidity);
            statement.setDate(1, expiration);
            statement.setString(2, clientId);
            return statement.executeUpdate();
        }
    }

    private Date getExpiration(final int validity) {
        Instant expiration = Instant.now().plusSeconds(validity).atOffset(UTC).toInstant();
        return new Date(from(expiration).getTime());
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
