FROM jjanzic/docker-python3-opencv

## Variables
ARG DIR=/srv
ARG IS_DEV_ENV=false

## Copy sources
RUN mkdir -p $DIR
COPY src $DIR/src
COPY test $DIR/test
COPY docker-entrypoint.sh $DIR/docker-entrypoint.sh
COPY docker-install.sh $DIR/docker-install.sh
COPY main.py $DIR/main.py
COPY init_mongo_db.py $DIR/init_mongo_db.py
COPY requirements.txt $DIR/requirements.txt
COPY uwsgi.ini $DIR/uwsgi.ini
COPY wait-for-it.sh $DIR/wait-for-it.sh
RUN chmod +x $DIR/docker-entrypoint.sh
RUN chmod +x $DIR/wait-for-it.sh
RUN chmod +x $DIR/init_mongo_db.py
RUN chmod +x $DIR/docker-install.sh
RUN mkdir $DIR/mongo_data

## Install dependencies
RUN $DIR/docker-install.sh $IS_DEV_ENV
RUN pip3 --no-cache-dir install -r $DIR/requirements.txt

## Entrypoint
WORKDIR $DIR
RUN ln -s $DIR /var/tmp/efrs_rootdir
ENTRYPOINT ["/var/tmp/efrs_rootdir/docker-entrypoint.sh"]
