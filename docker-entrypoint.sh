#!/bin/bash -e

export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONUNBUFFERED=0

./wait-for-it.sh mongo:27017
python3 ./init_mongo_db.py
service nginx start
uwsgi --ini uwsgi.ini
