#!/bin/bash

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