#!/bin/bash
IMG_DIR=$1

SCANNERS=${SCANNERS:-Facenet2018}
IMG_NAMES=${IMG_NAMES:-000_5.jpg 001_A.jpg 002_A.jpg 003_A.jpg 004_A.jpg 005_A.jpg 006_A.jpg}
MEM_LIMITS=${MEM_LIMITS:-1g}
IMG_LENGTH_LIMITS=${IMG_LENGTH_LIMITS:-640 99999}
SHOW_OUTPUT=${SHOW_OUTPUT:-true}

for scanner in ${SCANNERS/,/ }; do
  for mem_limit in ${MEM_LIMITS/,/ }; do
    for img_length_limit in ${IMG_LENGTH_LIMITS/,/ }; do
      for img_name in ${IMG_NAMES/,/ }; do
        # Run test
        OUTPUT=$(docker run --memory=$mem_limit --memory-swap=$mem_limit -e IMG_LENGTH_LIMIT=$img_length_limit -e SCANNER=$scanner -e IMG_NAME=$img_name "frs-core_ml${ID}" python -m tools.scan_faces 2>&1)
        EXIT_CODE=$?

        # Format output
        if [ "$SHOW_OUTPUT" = true ]; then
          if [[ ! -z "$OUTPUT" ]]; then
            NEWLINE=$'\n'
            OUTPUT="${NEWLINE}${OUTPUT}"
          fi
        else
          OUTPUT=
        fi

        # Format and print result
        MSG=$(identify -ping -format "%f ${img_length_limit}px $mem_limit %G %[size] ($scanner)\n" $IMG_DIR/$img_name)
        if [ $EXIT_CODE -eq 0 ]; then
          printf "[   OK   ] %s\n" "$MSG"
        else
          printf "[FAIL %-3s] %s%s\n" "$EXIT_CODE" "$MSG" "$OUTPUT"
        fi
      done
    done
  done
done
