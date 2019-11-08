#!/bin/bash -xe

## Parse arguments
DONT_BUILD_CONTAINERS=''
FREEZE_REQUIREMENTS=''
HOST='http://localhost:5001'

print_usage() {
  printf "
Builds containers and runs all tests against them.

Usage: ./%s [-d] [-f] [-h <HOST:PORT>]
Options:
    -d      Don't build Docker containers (useful when changes are made only outside the already built containers).
    -f      If tests run successfully, freeze versions and dependencies in requirements.txt (useful after manually adding new dependencies).
    -h      Specify host by which the started container can be accessed, for example: -h http://10.130.66.131:5001. Default is %s.
" "$(basename "$0")" "$HOST"
}

while getopts 'df:h' flag; do
  case "${flag}" in
  d) DONT_BUILD_CONTAINERS='true' ;;
  f) FREEZE_REQUIREMENTS='true' ;;
  h) HOST="$OPTARG" ;;
  *)
    print_usage
    exit 1
    ;;
  esac
done

## Install dependencies
curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose
chmod +x /usr/bin/docker-compose
echo "deb http://ftp.de.debian.org/debian testing main" >> /etc/apt/sources.list
echo 'APT::Default-Release "testing";' | sudo tee -a /etc/apt/apt.conf.d/00local
apt install dos2unix python3.6 python3-pip -y
ln -sf python3.7 /usr/bin/python

## Set Current Dir to the script's dir
cd "${0%/*}"

## Build and run docker containers
dos2unix ./* # File pre-processing (CRLF endings in certain files cause `docker-compose up` to crash)
if [ "$DONT_BUILD_CONTAINERS" != 'true' ]; then
  docker-compose build
fi
docker-compose up &
trap "docker-compose down" SIGINT SIGTERM EXIT

## Wait until successful start of the service with 60s timeout
export HOST
timeout 60 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $HOST/status)" != "200" ]]; do sleep 1; echo "$HOST/status"; done'

## Run tests from inside the container
docker exec ml python3 -m pytest -m "not integration" src
docker exec ml python3 -m pytest -m integration src

## Run E2E tests from outside the container
python -m pip install requests pytest pytest-ordering
if [ "$DONT_BUILD_CONTAINERS" = 'true' ]; then
  # If we're reusing mongo containers, drop and recreate the database
  python -m pytest -ra --verbose test_e2e/test_e2e.py --host "$HOST" --drop-db;
else
  python -m pytest -ra --verbose test_e2e/test_e2e.py --host "$HOST";
fi

## Freeze versions and dependencies in requirements.txt
if [ "$FREEZE_REQUIREMENTS" = 'true' ]; then
  docker exec ml pip freeze >requirements.txt
fi
