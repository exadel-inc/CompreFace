#!/bin/bash -e

skip_build=''
update_requirements=''

print_usage() {
  printf "
Builds a container locally and then runs e2e tests against it

Usage: ./run-e2e-tests-dev.sh [-s] [-u]
Options:
    -s      Use already built Docker containers
    -u      If tests run successfully, update requirements.txt
"
}

while getopts 'su' flag; do
  case "${flag}" in
    s) skip_build='true' ;;
    u) update_requirements='true' ;;
    *) print_usage
       exit 1 ;;
  esac
done

if [ "$skip_build" = 'true' ]; then echo skip; fi
if [ "$update_requirements" = 'true' ]; then echo upd; fi
