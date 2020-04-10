#!/bin/bash
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

COMMAND=$1
if [[ "$COMMAND" != "stop" && "$COMMAND" != "start" ]]; then
  print "ERROR: Valid command is not provided"
  exit 1
fi

if [ -f ${COMPOSE_PROJECT_NAME:-frs-core}.pid ]; then
  kill -9 "$(cat ${COMPOSE_PROJECT_NAME:-frs-core}.pid)"
fi

if [[ "$COMMAND" == "start" ]]; then
  (
    python -m src.app &
    echo $! >${COMPOSE_PROJECT_NAME:-frs-core}.pid
    wait
    rm -f ${COMPOSE_PROJECT_NAME:-frs-core}.pid
  ) &
  exit 0
fi
