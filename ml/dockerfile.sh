#!/bin/bash -xe
cd "${0%/*}" || exit 1  # Set Current Dir to the script's dir

python -m pip --no-cache-dir install -r requirements.txt -e srcext/insightface/python-package
imageio_download_bin freeimage

apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" \
  | tee /etc/apt/sources.list.d/mongodb-org-4.0.list
apt-get update
apt-get install -y mongodb-org-tools mongodb-org-shell python-pymongo

if [ "$SKIP_TESTS" != true ]; then
  python -m pytest /app/ml/src
  python -m pylama --options /app/ml/pylama.ini /app/ml/src
else
  # If not using regular tests, at least run use smoke tests. Running scan with InsightFace also triggers the library to cache the models locally for subsequent use.
  export IMG_NAMES=000_5.jpg SHOW_IMG=false
  SCANNER=Facenet2018 \
    python -m tools.facescan.scan.run
  SCANNER=InsightFace \
    python -m tools.facescan.scan.run
fi;
