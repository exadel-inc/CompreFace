#!/bin/bash
IMG_DIR=$1

SCANNERS=${SCANNERS:-Facenet2018 InsightFace}
IMAGE_NAMES=${IMAGE_NAMES:-five-faces.jpg personD-img1.jpg personD-img2.jpg personD-img3.jpg personD-img4.jpg)}
MEM_LIMITS=${MEM_LIMITS:-1g 2g 3g 4g 5g 6g 7g 8g}

for scanner in ${SCANNERS/,/ }; do
  for image_name in ${IMAGE_NAMES/,/ }; do
    for mem_limit in ${MEM_LIMITS/,/ }; do
      OUTPUT=$(docker run --memory=$mem_limit --memory-swap=$mem_limit "frs-core_ml${ID}" python -m tools.scan_img "$scanner" "$image_name" 2>&1)
      EXIT_CODE=$?
      MSG=$(identify -ping -format "%f $mem_limit %G %[size] ($scanner)\n" $IMG_DIR/$image_name)
      if [ $EXIT_CODE -eq 0 ]; then
        printf "[   OK   ] %s\n" "$MSG"
      else
        printf "[FAIL %3s] %s%s\n" "$EXIT_CODE" "$MSG" "$OUTPUT"
      fi
    done
  done
done
