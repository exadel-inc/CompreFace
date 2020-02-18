FROM jjanzic/docker-python3-opencv

## Variables
ARG DIR=/srv
ARG IS_DEV_ENV=false


ENV UWSGI_MAX_LIFETIME 3600
ENV UWSGI_MEMORY_REPORT true
ENV UWSGI_MAX_REQUESTS 10000
ENV UWSGI_RELOAD_ON_RSS 2500
ENV UWSGI_WORKER_RELOAD_MERCY 60
ENV UWSGI_LIMIT_AS 3072


## Copy sources
RUN mkdir -p $DIR
COPY src $DIR/src
COPY models $DIR/models
COPY test_files $DIR/test_files
COPY main.py $DIR/main.py
COPY docker-entrypoint.sh $DIR/docker-entrypoint.sh
COPY wait-for-it.sh $DIR/wait-for-it.sh
COPY uwsgi.ini $DIR/uwsgi.ini
COPY requirements.txt $DIR/requirements.txt
COPY init_mongo_db.py $DIR/init_mongo_db.py
COPY install-dependencies.sh $DIR/install-dependencies.sh
RUN chmod +x $DIR/docker-entrypoint.sh
RUN chmod +x $DIR/wait-for-it.sh
RUN chmod +x $DIR/init_mongo_db.py
RUN chmod +x $DIR/install-dependencies.sh
RUN mkdir $DIR/mongo_data

## Install dependencies
RUN $DIR/install-dependencies.sh $IS_DEV_ENV
RUN pip3 --no-cache-dir install -r $DIR/requirements.txt


## Entrypoint
WORKDIR $DIR
RUN ln -s $DIR /var/tmp/efrs_rootdir
ENTRYPOINT ["/var/tmp/efrs_rootdir/docker-entrypoint.sh", "--UWSGI_MAX_LIFETIME", "${UWSGI_MAX_LIFETIME}", "--UWSGI_MEMORY_REPORT", "--UWSGI_MAX_REQUESTS", "${UWSGI_MAX_REQUESTS}", "--UWSGI_RELOAD_ON_RSS", "${UWSGI_RELOAD_ON_RSS}", "--UWSGI_WORKER_RELOAD_MERCY", "${UWSGI_WORKER_RELOAD_MERCY}", "--UWSGI_LIMIT_AS", "${UWSGI_LIMIT_AS}"]
