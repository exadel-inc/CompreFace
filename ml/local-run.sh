#!/bin/bash
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

(
  python -m src.main &
  echo $! >local-run.ml.pid
  wait
  rm -f local-run.ml.pid
) &
