#!/bin/bash
IMG_DIR=$1

SCANNERS=${SCANNERS:-Facenet2018 InsightFace}
IMAGE_NAMES=${IMAGE_NAMES:-BAD_IMG_20200213_114434_Mi.jpg GOOD_photo5343555498458328037_S.jpg IMG_20190828_194828_1.jpg IMG_20191205_120559.jpg IMG_20191218_195158.jpg five-faces.jpg personD-img1.jpg personD-img2.jpg personD-img3.jpg personD-img4.jpg}
MEM_LIMITS=${MEM_LIMITS:-1g 2g 3g 4g 6g 8g}
IMG_LENGTH_LIMITS=${IMG_LENGTH_LIMITS:-1000 99999}
SHOW_OUTPUT=${SHOW_OUTPUT:-true}

for scanner in ${SCANNERS/,/ }; do
  for image_name in ${IMAGE_NAMES/,/ }; do
    for mem_limit in ${MEM_LIMITS/,/ }; do
      for img_length_limit in ${IMG_LENGTH_LIMITS/,/ }; do
        # Run test
        OUTPUT=$(docker run --memory=$mem_limit --memory-swap=$mem_limit -e IMG_LENGTH_LIMIT=$img_length_limit "frs-core_ml${ID}" python -m tools.scan_faces "$scanner" "$image_name" 2>&1)
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
        MSG=$(identify -ping -format "%f ${img_length_limit}px $mem_limit %G %[size] ($scanner)\n" $IMG_DIR/$image_name)
        if [ $EXIT_CODE -eq 0 ]; then
          printf "[   OK   ] %s\n" "$MSG"
        else
          printf "[FAIL %-3s] %s%s\n" "$EXIT_CODE" "$MSG" "$OUTPUT"
        fi
      done
    done
  done
done
