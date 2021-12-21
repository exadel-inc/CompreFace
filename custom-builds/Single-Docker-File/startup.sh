#!/bin/bash

# EXTERNAL_DB defines if we need to run internal DB
external_db=${EXTERNAL_DB:-false}
if [ "$external_db" = false ] ; then
    # restore default data if it was cleared by volume creation
    if [ -z "$(ls -A $PGDATA)" ]; then
       echo "Postgres directory is empty. Copy default values into it"
       cp -r /var/lib/postgresql/default/* $PGDATA
    fi
    # change permissions in case they were corrupted
    chown -R postgres:postgres $PGDATA
    chmod 700 $PGDATA

    echo Starting compreface-postgres-db
    supervisorctl start compreface-postgres-db
fi

# wait until DB starts
sleep 10
echo Starting compreface-admin
supervisorctl start compreface-admin

# wait until compreface-admin make all migrations
sleep 10
echo Starting compreface-api
supervisorctl start compreface-api

# wait until compreface-admin starts
sleep 10
echo Starting compreface-fe
supervisorctl start compreface-fe