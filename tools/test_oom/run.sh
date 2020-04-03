#!/bin/bash
IMG_DIR=$1

backends=(InsightFace)
image_names=(five-faces.jpg personD-img1.jpg personD-img2.jpg personD-img3.jpg personD-img4.jpg)
mem_limits=(1g 2g 3g 4g 5g 6g 7g 8g)

for scanner in "${backends[@]}"; do
  for image_name in "${image_names[@]}"; do
    for mem_limit in "${mem_limits[@]}"; do
      OUTPUT=$(docker run --memory=$mem_limit --memory-swap=$mem_limit frs-core_ml python -m tools.scan_img $backend $image_name)
      EXIT_CODE=$?
      MSG=$(identify -ping -format "%f $mem_limit %G %[size] ($backend)\n" $IMG_DIR/$image_name)
      if [ $EXIT_CODE -eq 0 ]; then
        printf "==========[   OK   ] %s\n" "$MSG"
      else
        printf "==========[FAIL %3s] %s%s\n" "$EXIT_CODE" "$MSG" "$OUTPUT"
      fi
    done
  done
done
