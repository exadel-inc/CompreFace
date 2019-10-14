#!/bin/bash -xe

# File pre-processing (container crashes with CRLF endings in certain files)
dos2unix ./*

# Build and launch containers
docker-compose build
docker-compose up &
sleep 60s

# Run test
curl --silent --fail http://localhost:5001/status

# Teardown
docker-compose down

echo "=== DOCKER BUILD TEST: SUCCESS ==="
