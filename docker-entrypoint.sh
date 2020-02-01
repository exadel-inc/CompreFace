#!/bin/bash -e

export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONUNBUFFERED=0

./wait-for-it.sh mongo:27017
python3 ./init_mongo_db.py

export memory="$UWSGI_MEMORY_REPORT"
export lifetime="$UWSGI_MAX_LIFETIME"
export requests="$UWSGI_MAX_REQUESTS"
export memory_rss="$UWSGI_RELOAD_ON_RSS"
export reload_mercy="$UWSGI_WORKER_RELOAD_MERCY"
export memory_limit="$UWSGI_LIMIT_AS"

uwsgi --ini uwsgi.ini --max-worker-lifetime "$lifetime" --memory-report  --max-requests "$requests"  --reload-on-rss "$memory_rss" --worker-reload-mercy "$reload_mercy" --limit-as "$memory_limit"

