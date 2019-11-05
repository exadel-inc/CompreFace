#!/bin/bash -xe
print_usage() {
  printf "
Builds containers and runs all tests against them

Usage: ./build-and-test.sh [-d] [-f]
Options:
    -d      Don't build Docker containers (useful when changes are made only outside the already built containers)
    -f      If tests run successfully, freeze versions and dependencies in requirements.txt (useful after manually adding new dependencies)
"
}

## Parse arguments
DONT_BUILD_CONTAINERS=''
FREEZE_REQUIREMENTS=''

curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose
chmod +x /usr/bin/docker-compose 
apt install dos2unix -y 

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
#timeout 60 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $HOST/status)" != "200" ]]; do sleep 1; echo "$HOST/status"; done'
sleep 60

## Run tests from inside the container
docker exec ml python3 -m pytest -m "not integration" -ra --verbose src
docker exec ml python3 -m pytest -m integration -ra --verbose src

## Run E2E tests from outside the container
python -m pip install requests pytest pytest-ordering
python -m pytest --host $HOST -ra --verbose test_e2e/test_e2e.py

## Freeze versions and dependencies in requirements.txt
if [ "$FREEZE_REQUIREMENTS" = 'true' ]; then
  docker exec ml pip freeze >requirements.txt
fi
