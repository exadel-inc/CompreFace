FROM jjanzic/docker-python3-opencv
WORKDIR /srv

## Copy sources
COPY src src
COPY srcext srcext
COPY sample_images sample_images
COPY tools tools
COPY requirements.txt .
COPY pytest.ini .
COPY pylama.ini .
COPY uwsgi.ini .

## Prepare environment
RUN python -m pip --no-cache-dir install -r requirements.txt
RUN python -m pip --no-cache-dir install -e srcext/insightface/python-package
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0

## Run tests
ARG DO_RUN_TESTS
RUN if [ "$DO_RUN_TESTS" = true ]; then \
    python -m pytest /srv/src && \
    python -m pylama --options /srv/pylama.ini /srv/src; \
    fi

## Run smoke test and Cache downloadable dependencies for Scanner backends
RUN python -m tools.scan_img Facenet2018 five-faces.jpg &>/dev/null
RUN python -m tools.scan_img InsightFace five-faces.jpg &>/dev/null

## Runtime
CMD ["uwsgi", "--ini", "uwsgi.ini"]
