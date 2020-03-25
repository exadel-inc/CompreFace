FROM jjanzic/docker-python3-opencv
WORKDIR /srv

## Copy sources
COPY src src
COPY srcext srcext
COPY requirements.txt .
COPY pytest.ini .
COPY pylama.ini .
COPY docker-entrypoint.sh .
COPY uwsgi.ini .

## Prepare environment
RUN chmod +x docker-entrypoint.sh
RUN python3 -m pip --no-cache-dir install -r requirements.txt
RUN python3 -m pip --no-cache-dir install -e srcext/insightface/python-package

## Run tests
ARG DO_RUN_TESTS
RUN if [ "$DO_RUN_TESTS" = true ] ; then \
    python3 -m pytest /srv/src && \
    python3 -m pylama --options /srv/pylama.ini /srv/src; \
    fi

## Entrypoint
ENTRYPOINT ["/srv/docker-entrypoint.sh"]
