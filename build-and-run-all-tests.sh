#!/bin/bash -xe
print_usage() {
  printf "
Builds a container locally and then runs tests

Usage: ./run-e2e-tests-dev.sh [-s] [-u]
Options:
    -d      Don't build Docker containers
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

## Build docker containers
dos2unix ./* # File pre-processing (CRLF endings in certain files cause `docker-compose up` to crash)
if [ "$DONT_BUILD_CONTAINERS" != 'true' ]; then
  docker-compose build
fi
docker-compose up &
trap "docker-compose down" SIGINT SIGTERM EXIT

############################
###### START OF TESTS ######
############################

## TEST 1 - Smoke test (Allow up to 1h (=3600s) for the service to start)
export HOST=http://localhost:5001
timeout 3600 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $HOST/status)" != "200" ]]; do sleep 1; done'

## TEST 2 - Unit tests
docker exec ml python3 -m pytest src

## TEST 3 - E2E tests
set_up_venv_and_activate() {
  python -m pip install --user virtualenv
  python -m venv venv
  if [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    source venv/bin/activate # In Linux
  else
    venv/Scripts/activate # In Windows
  fi
  python -m pip install \
    requests \
    pytest
}
set_up_venv_and_activate
python -m pytest --host $HOST test_e2e/test_e2e.py
deactivate

##########################
###### END OF TESTS ######
##########################

## Freeze versions and dependencies in requirements.txt
if [ "$FREEZE_REQUIREMENTS" = 'true' ]; then
  docker exec ml pip freeze >requirements.txt
fi
