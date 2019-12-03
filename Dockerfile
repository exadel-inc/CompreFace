FROM jjanzic/docker-python3-opencv

## Variables
ARG DIR=/srv
ARG IS_DEV_ENV=false

## Copy sources
RUN mkdir -p $DIR
COPY src $DIR/src
COPY models $DIR/models
COPY test_files $DIR/test_files
COPY main.py $DIR/main.py
COPY docker-entrypoint.sh $DIR/docker-entrypoint.sh
COPY uwsgi.ini $DIR/uwsgi.ini
COPY requirements.txt $DIR/requirements.txt
COPY install-dependencies.sh $DIR/install-dependencies.sh
RUN chmod +x $DIR/docker-entrypoint.sh
RUN chmod +x $DIR/install-dependencies.sh
RUN mkdir $DIR/mongo_data

## Install dependencies
RUN $DIR/install-dependencies.sh $IS_DEV_ENV
RUN pip3 --no-cache-dir install -r $DIR/requirements.txt

## Expose port for uWSGI
EXPOSE 3000

## Entrypoint
WORKDIR $DIR
RUN ln -s $DIR /var/tmp/efrs_rootdir
ENTRYPOINT ["/var/tmp/efrs_rootdir/docker-entrypoint.sh"]
