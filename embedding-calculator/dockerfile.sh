#!/bin/bash -xe
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

python -m pip --no-cache-dir install -r requirements.txt -e srcext/insightface/python-package
imageio_download_bin freeimage

if [ "$SKIP_TESTS" != true ]; then
  python -m pylama --options /app/ml/pylama.ini /app/ml/src
  python -m pytest /app/ml/src
else
  # InsightFace downloads and caches models locally upon first run
  SCANNER=InsightFace IMG_NAMES=000_5.jpg SAVE_IMG=false \
    python -m tools.scan
fi
