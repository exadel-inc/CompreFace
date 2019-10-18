#!/bin/bash -xe
print_usage() {
  printf "
Builds a container locally and then runs e2e tests against it

Usage: ./run-e2e-tests-dev.sh [-s] [-u]
Options:
    -r      Reuse existing Docker containers (don't rebuild)
    -f      If tests run successfully, freeze versions and dependencies in requirements.txt (useful after manually adding new dependencies)
"
}

## Parse arguments
REUSE_EXISTING_CONTAINERS=''
FREEZE_REQUIREMENTS=''

while getopts 'rf' flag; do
  case "${flag}" in
  r) REUSE_EXISTING_CONTAINERS='true' ;;
  f) FREEZE_REQUIREMENTS='true' ;;
  *)
    print_usage
    exit 1
    ;;
  esac
done

cd "${0%/*}" # Set Current Dir to the script's dir

## Set up Docker containers
dos2unix ./* # File pre-processing (CRLF endings in certain files cause `docker-compose up` to crash)
if [ "$REUSE_EXISTING_CONTAINERS" != 'true' ]; then
  docker-compose build
fi
docker-compose up &
trap "docker-compose down" SIGINT SIGTERM EXIT

## Run E2E tests
./run-e2e-tests.sh -h http://localhost:5001

## Freeze versions and dependencies in requirements.txt
if [ "$FREEZE_REQUIREMENTS" = 'true' ]; then
  docker exec ml pip freeze >requirements.txt
fi
