#!/bin/bash
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

COMMAND=$1
PORT=$2
if [[ "$COMMAND" != "stop" && "$COMMAND" != "start" ]]; then
  print "ERROR: Valid command is not provided"
  exit 1
fi

if [ -f run.pid ]; then
  kill -9 "$(cat run.pid)"
fi

if [[ "$COMMAND" == "start" ]]; then
  (
    python -m src.app "$PORT" &
    echo $! >run.pid
    wait
    rm -f run.pid
  ) &
  exit 0
fi
