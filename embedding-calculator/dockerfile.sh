#!/bin/bash -xe
cd "${0%/*}" || exit 1 # Set Current Dir to the script's dir

python -m pip --no-cache-dir install -r requirements.txt -e srcext/insightface/python-package
imageio_download_bin freeimage

if [ "$SKIP_TESTS" != true ]; then
  # Runs lint checks
  python -m pylama --options /app/ml/pylama.ini /app/ml/src /app/ml/tools
  # Runs unit and integration tests
  python -m pytest -m "not performance" /app/ml/src /app/ml/tools
else
  # InsightFace downloads and caches models locally upon first run
  SCANNER=InsightFace IMG_NAMES=000_5.jpg SAVE_IMG=false \
    python -m tools.scan
fi
