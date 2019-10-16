#!/bin/bash -xe
####################
# Runs e2e tests against an already running container
# Usage:             ./run-e2e-tests.sh http://<HOST>:<PORT>
# Usage example:     ./run-e2e-tests.sh http://localhost:5001
####################

if [ -z "$1" ]; then
  echo "Incorrect usage. Usage example: ./run-e2e-tests.sh http://localhost:5001"
  exit 1
fi

export HOST=$1
cd "${0%/*}" # Change Current Dir to the script's dir

## Set up Python environment
python -m pip install --user virtualenv
python -m venv test_e2e/env
if [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  source test_e2e/env/bin/activate # In Linux
else
  test_e2e/env/Scripts/activate # In Windows
fi
python -m pip install requests pytest

## Run e2e tests
timeout 600 bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' $HOST/status)" != "200" ]]; do sleep 1; done' || false
python -m pytest --host $HOST test_e2e/test_e2e.py

## Teardown
deactivate
