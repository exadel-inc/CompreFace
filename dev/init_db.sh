#!/bin/sh
su -u postgres

psql <<- EOSQL
    SELECT 'CREATE USER postgres superuser with password postgres'
    WHERE NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'postgres');

    SELECT 'CREATE DATABASE frs'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'frs');

    GRANT ALL PRIVILEGES ON DATABASE frs TO postgres;
EOSQL