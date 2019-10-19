#!/bin/bash

./wait-for-it.sh mongo:27017

result=$(mongo "mongo/recognition" --eval "db.getCollection('models.files').count({'filename': '20170512-110547.pb'})" | tail -n 1)
if [[ $result == "1" ]]; then
  echo 'Mongo DB is not empty'
else
  echo 'Mongo DB is empty, initializing'
  $(mongorestore --gzip --archive=dump.archive --host=mongo --db=recognition)
fi

export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONUNBUFFERED=0

service nginx start
uwsgi --ini uwsgi.ini
