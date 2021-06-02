#!/bin/bash
CONCURRENCY=${1:-1}
ENDPOINT=${2:-http://localhost:3000/scan_faces}
declare -a IMAGES=("008_B.jpg" "001_A.jpg")

# install apache benchmark
ab -V > /dev/null || (apt-get update && apt-get install -y apache2-utils)

# run benchmark
for IMAGE in "${IMAGES[@]}"
do
  IMAGE="./sample_images/$IMAGE"
  [ ! -f "$IMAGE" ] && echo "Image ${IMAGE} not found" && exit
  FILESIZE=$(stat -c%s "$IMAGE")
  IMAGE_B64=$(base64 $IMAGE)
  echo -e "--image_file\r\nContent-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\nContent-Transfer-Encoding: base64\r\n\r\n${IMAGE_B64}\r\n--image_file--" > post.data
  echo "--- Run benchmark with $IMAGE ($FILESIZE bytes) ---"

  RESPONSE=$(ab -n 1 -c 1 -v 4 -p post.data -T "multipart/form-data; boundary=image_file" $ENDPOINT | grep embedding)
  [[ -z $RESPONSE ]] && echo "Error: No embedding in response" && exit 1

  ab -n 20 -c $CONCURRENCY -p post.data -T "multipart/form-data; boundary=image_file" $ENDPOINT \
    | grep 'per request\|Concurrency'
  echo
done

rm post.data