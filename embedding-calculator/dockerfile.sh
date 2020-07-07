#!/bin/bash -xe
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

python -m pip --no-cache-dir install -r requirements.txt -e srcext/insightface/python-package
imageio_download_bin freeimage

if [ "$SKIP_TESTS" != true ]; then
  # Runs unit and integration tests
  python -m pytest -m "not performance" /app/ml/src
else
  if [[ $SCANNER == *"InsightFace"* ]]; then
    # InsightFace downloads and caches models locally upon first run
    SCANNER=InsightFace IMG_NAMES=000_5.jpg SAVE_IMG=false python -m tools.scan
  fi
fi
