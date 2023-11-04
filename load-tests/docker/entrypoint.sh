#!/bin/sh

FOLDERS=""

if [ -z "$TESTS" ]
then
  echo "TESTS is empty, run all tests"
  FOLDERS=$(ls -F tests | tr "/ " "\n")
else
  echo "TESTS to run: $TESTS"
  FOLDERS=$(echo $TESTS | tr ";" "\n")
fi

for test_folder in $FOLDERS
do
  echo "************************************************************************"
  echo "********************************** $test_folder"
  echo "************************************************************************"

  chmod +x ./tests/${test_folder}/loadtest.k6.js
  ./k6 run \
    --insecure-skip-tls-verify \
    --vus ${VUS} \
    --iterations ${ITERATIONS} \
    --duration ${DURATION} \
    -e HOSTNAME="$HOSTNAME" \
    -e DB_CONNECTION_STRING="$DB_CONNECTION_STRING" \
    --out influxdb=${INFLUXDB_HOSTNAME}/${test_folder} \
    ./tests/${test_folder}/loadtest.k6.js
done
