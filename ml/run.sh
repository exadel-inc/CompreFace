#!/bin/bash
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

COMMAND=$1

if [[ "$COMMAND" == "start" ]]; then
  (
    python -m src.app &
    echo $! >run.pid
    wait
    rm -f run.pid
  ) &
  exit 0
fi

if [[ "$COMMAND" == "stop" ]]; then
  kill -9 "$(cat run.pid)"
  exit 0
fi

exit 1
