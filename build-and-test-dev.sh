#!/bin/bash -xe
print_usage() {
  printf "
Builds containers and runs all tests against them. For use in a local development environment.

Usage: ./build-and-test.sh [-d] [-f]
Options:
    -d      Don't build Docker containers (useful when changes are made only outside the already built containers)
    -f      If tests run successfully, freeze versions and dependencies in requirements.txt (useful after manually adding new dependencies)
"
}

## Parse arguments
DONT_BUILD_CONTAINERS=''
FREEZE_REQUIREMENTS=''

while getopts 'df' flag; do
  case "${flag}" in
  d) DONT_BUILD_CONTAINERS='true' ;;
  f) FREEZE_REQUIREMENTS='true' ;;
  *)
    print_usage
    exit 1
    ;;
  esac
done

cd "${0%/*}" # Set Current Dir to the script's dir

## Build and run docker containers
dos2unix ./* # File pre-processing (CRLF endings in certain files cause `docker-compose up` to crash)
if [ "$DONT_BUILD_CONTAINERS" != 'true' ]; then
  docker-compose build
fi
docker-compose up &
trap "docker-compose down" SIGINT SIGTERM EXIT

## Wait until successful start of the service with 60s timeout
export HOST=http://localhost:5001
timeout 60 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $HOST/status)" != "200" ]]; do sleep 1; done'

## Run tests from inside the container
docker exec ml python3 -m pytest -m "not integration" src
docker exec ml python3 -m pytest -m integration src

## Run E2E tests from outside the container
python -m pip install requests pytest pytest-ordering
if [ "$DONT_BUILD_CONTAINERS" = 'true' ]; then
  # If we're reusing mongo containers, drop and recreate the database
  python -m pytest -ra --verbose test_e2e/test_e2e.py --host $HOST --drop-db;
else
  python -m pytest -ra --verbose test_e2e/test_e2e.py --host $HOST;
fi

## Freeze versions and dependencies in requirements.txt
if [ "$FREEZE_REQUIREMENTS" = 'true' ]; then
  docker exec ml pip freeze >requirements.txt
fi
