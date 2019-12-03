#!/bin/bash -e

export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONUNBUFFERED=0

python3 ./init_mongo_db.py
uwsgi --ini uwsgi.ini
