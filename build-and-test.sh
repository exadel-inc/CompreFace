#!/bin/bash -xe

BUILD_SERVER_DEFAULT_HOST='http://10.130.66.131:3000'
DEV_ENV_DEFAULT_HOST='http://localhost:3000'

## Parse arguments
print_usage() {
  printf "
Builds containers and runs all tests against them.

Usage: ./%s [-d] [-e] [-h <HOST:PORT>]
Options:
    -d      Run with settings for a local development environment (instead of a build server)
    -e      Use already built docker containers (don't rebuild)
    -h      Specify host by which the started container can be accessed, for example: -h http://localhost:3000
" "$(basename "$0")"
}

IS_DEV_ENV='false'
USE_EXISTING_CONTAINERS='false'
HOST=''

while getopts 'deh:' flag; do
  case "${flag}" in
  d) IS_DEV_ENV='true' ;;
  e) USE_EXISTING_CONTAINERS='true' ;;
  h) HOST="$OPTARG" ;;
  *)
    print_usage
    exit 1
    ;;
  esac
done

if [ -z "$HOST" ]; then
  if [ "$IS_DEV_ENV" = 'true' ]; then
    HOST=$DEV_ENV_DEFAULT_HOST
  else
    HOST=$BUILD_SERVER_DEFAULT_HOST
  fi
fi

## Install dependencies
if [ "$IS_DEV_ENV" = 'false' ]; then
  which docker-compose || curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose && chmod +x /usr/bin/docker-compose
  grep -q 'deb http://ftp.de.debian.org/debian testing main' /etc/apt/sources.list || echo "deb http://ftp.de.debian.org/debian testing main" >>/etc/apt/sources.list
  grep -q 'APT::Default-Release "testing";' /etc/apt/apt.conf.d/00local || echo 'APT::Default-Release "testing";' | sudo tee -a /etc/apt/apt.conf.d/00local
  apt update && apt install dos2unix python3.7 python3-pip -y
  ln -sf python3.7 /usr/bin/python
fi
python -m pip install -r requirements-test.txt

## Set Current Dir to the script's dir
cd "${0%/*}"

## Build and run docker containers
dos2unix ./* # File pre-processing (CRLF endings in certain _files cause `docker-compose up` to crash)
if [ "$USE_EXISTING_CONTAINERS" = 'false' ]; then
  docker-compose build --build-arg IS_DEV_ENV="$IS_DEV_ENV"
fi
docker-compose up &
trap "docker-compose down" SIGINT SIGTERM EXIT

## Wait until successful start of the service with 60s timeout
export HOST
if [ "$IS_DEV_ENV" = 'true' ]; then
  timeout 60 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $HOST/status)" != "200" ]]; do sleep 1; echo "Waiting for 200 response from $HOST/status"; done'
else
  sleep 60
fi

## Run tests from inside the container
docker exec ml python3 -m pytest -m "not integration" src
docker exec ml python3 -m pytest -m integration src

## Run E2E tests from outside the container
if [ "$USE_EXISTING_CONTAINERS" = 'true' ]; then
  # If we're reusing database containers, drop and recreate the databases
  python -m pytest -ra --verbose test/e2e/e2e.py --host "$HOST" --drop-db
else
  python -m pytest -ra --verbose test/e2e/e2e.py --host "$HOST"
fi

## Freeze versions and dependencies in requirements.txt
if [ "$IS_DEV_ENV" = 'true' ]; then
  docker exec ml pip freeze >requirements.txt
fi
