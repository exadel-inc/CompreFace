FROM jjanzic/docker-python3-opencv
WORKDIR /srv
ENV HOME=/srv

## Copy sources
COPY sample_images sample_images
COPY src src
COPY srcext srcext
COPY pylama.ini .
COPY pytest.ini .
COPY requirements.txt .
COPY uwsgi.ini .
COPY .sonarqubeignore .

## Prepare environment
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0
ARG BE_VERSION
ENV BE_VERSION=$BE_VERSION
ARG APP_VERSION_STRING
ENV APP_VERSION_STRING=$APP_VERSION_STRING
RUN mkdir mongo_data
RUN python -m pip --no-cache-dir install -r requirements.txt
RUN imageio_download_bin freeimage
RUN python -m pip --no-cache-dir install -e srcext/insightface/python-package
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
RUN echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.0.list
RUN apt-get update
RUN apt-get install -y mongodb-org-tools mongodb-org-shell python-pymongo

## Run tests
ARG SKIP_TESTS
ARG FORCE_FAIL_UNIT_TESTS
RUN if [ "$SKIP_TESTS" != true ]; then FORCE_FAIL_UNIT_TESTS=$FORCE_FAIL_UNIT_TESTS python -m pytest /srv/src; fi
RUN if [ "$SKIP_TESTS" != true ]; then python -m pylama --options /srv/pylama.ini /srv/src; fi

## Run smoke test and Cache downloadable dependencies for Scanner backends
RUN SCANNER=Facenet2018 IMG_NAMES=000_5.jpg SHOW_IMG=false python -m src.services.facescan.run
# (TODO EFRS-435: add this line ->) RUN SCANNER=InsightFace IMG_NAMES=000_5.jpg SHOW_IMG=false python -m src.services.facescan.run

## Runtime
EXPOSE 3000
CMD ["uwsgi", "--ini", "uwsgi.ini"]
