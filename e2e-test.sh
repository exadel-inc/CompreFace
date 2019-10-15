#!/bin/bash -xe
# ===============================
# e2e-test.sh
# Manually launched containerization and local deployment test
# ===============================

# File pre-processing (CRLF endings in certain files cause `docker-compose up` to crash)
dos2unix ./*

# Build and launch containers
docker-compose build
docker-compose up &

# Run test
timeout 600 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' http://localhost:5001/status)" != "200" ]]; do sleep 1; done' || false

# Teardown
docker-compose down

# Confirm test result
echo "TEST IS SUCCESSFUL"
