#!/bin/bash
if [ $# -eq 0 ]; then
  exit 1
fi

(
  python -m src.main &
  echo $! >$1
  wait
  rm -f $1
) &
