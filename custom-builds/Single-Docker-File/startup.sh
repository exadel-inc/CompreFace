#!/bin/bash

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