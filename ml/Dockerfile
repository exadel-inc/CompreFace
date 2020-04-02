FROM jjanzic/docker-python3-opencv
WORKDIR /srv

## Copy sources
COPY src src
COPY srcext srcext
COPY requirements.txt .
COPY pytest.ini .
COPY pylama.ini .
COPY uwsgi.ini .

## Prepare environment
RUN python3 -m pip --no-cache-dir install -r requirements.txt
RUN python3 -m pip --no-cache-dir install -e srcext/insightface/python-package
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0

## Run tests
ARG DO_RUN_TESTS
RUN if [ "$DO_RUN_TESTS" = true ] ; then \
    python3 -m pytest /srv/src && \
    python3 -m pylama --options /srv/pylama.ini /srv/src; \
    fi

## Runtime
CMD ["uwsgi", "--ini", "uwsgi.ini"]
