create table if not exists oauth_access_token
(
    token_id          VARCHAR(256),
    token             bytea,
    authentication_id VARCHAR(256) PRIMARY KEY,
    user_name         VARCHAR(256),
    client_id         VARCHAR(256),
    authentication    bytea,
    refresh_token     VARCHAR(256)
);

create table if not exists oauth_refresh_token
(
    token_id       VARCHAR(256),
    token          bytea,
    authentication bytea
);