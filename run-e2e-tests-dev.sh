#!/bin/bash -xe
####################
# Builds a container locally and then runs e2e tests against it
# Usage:            ./run-e2e-tests-dev.sh [--skip-build]
# Usage example:    ./run-e2e-tests-dev.sh --skip-build && echo "E2E TEST: PASSED!" || echo "E2E TEST: Failed."
# Options:
#     --skip-build       script will not rebuild the containers
####################

DO_DOCKER_BUILD=true
if [ "$1" = --skip-build ]; then DO_DOCKER_BUILD=false; fi

cd "${0%/*}" # Change Current Dir to the script's dir

## Set up Docker containers
dos2unix ./* # File pre-processing (CRLF endings in certain files cause `docker-compose up` to crash)
if [ "$DO_DOCKER_BUILD" = true ]; then docker-compose build; fi
docker-compose up &
trap "docker-compose down" SIGINT SIGTERM EXIT

## Run e2e tests
./run-e2e-tests.sh http://localhost:5001
