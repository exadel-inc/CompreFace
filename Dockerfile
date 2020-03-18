FROM jjanzic/docker-python3-opencv

## Variables
ARG DIR=/srv
ARG IS_DEV_ENV=false

## Copy sources
RUN mkdir -p $DIR
COPY src $DIR/src
COPY test $DIR/test
COPY sample_images $DIR/sample_images
COPY docker-entrypoint.sh $DIR/docker-entrypoint.sh
COPY docker-install.sh $DIR/docker-install.sh
COPY requirements.txt $DIR/requirements.txt
COPY uwsgi.ini $DIR/uwsgi.ini
COPY pytest.ini $DIR/pytest.ini
COPY Makefile $DIR/Makefile
RUN chmod +x $DIR/docker-entrypoint.sh
RUN chmod +x $DIR/docker-install.sh

## Install dependencies
RUN $DIR/docker-install.sh $IS_DEV_ENV
RUN pip3 --no-cache-dir install -r $DIR/requirements.txt
RUN pip3 --no-cache-dir install -e $DIR/src/shared/extlib/insightface/python-package

## Entrypoint
WORKDIR $DIR
RUN ln -s $DIR /var/tmp/efrs_rootdir
ENTRYPOINT ["/var/tmp/efrs_rootdir/docker-entrypoint.sh"]
