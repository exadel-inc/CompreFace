ARG BASE_IMAGE
FROM ${BASE_IMAGE:-python:3.7-slim}

RUN apt-get update && apt-get install -y build-essential cmake git wget unzip \
        curl yasm pkg-config libswscale-dev libtbb2 libtbb-dev libjpeg-dev \
        libpng-dev libtiff-dev libavformat-dev libpq-dev libfreeimage3 \
    && rm -rf /var/lib/apt/lists/*

# install common python packages
SHELL ["/bin/bash", "-c"]
WORKDIR /app/ml
COPY requirements.txt pytest.ini uwsgi.ini entrypoint.sh ./
RUN chmod +x entrypoint.sh && \
    pip --no-cache-dir install -r requirements.txt

ARG BE_VERSION
ARG APP_VERSION_STRING
ENV BE_VERSION=$BE_VERSION \
    APP_VERSION_STRING=$APP_VERSION_STRING \
    HOME=/app/ml \
    LC_ALL=C.UTF-8 \
    LANG=C.UTF-8 \
    PYTHONUNBUFFERED=0 \
    JOBLIB_MULTIPROCESSING=0

# download ML models
ARG RECOGNITION_OPTIMIZATION=no
ARG GPU_IDX=-1
ARG FACE_DETECTION_PLUGIN="facenet.FaceDetector"
ARG CALCULATION_PLUGIN="facenet.Calculator"
ARG EXTRA_PLUGINS="facenet.LandmarksDetector,agegender.AgeDetector,agegender.GenderDetector"
ENV RECOGNITION_OPTIMIZATION=$RECOGNITION_OPTIMIZATION \
    GPU_IDX=$GPU_IDX \
    FACE_DETECTION_PLUGIN=$FACE_DETECTION_PLUGIN \
    CALCULATION_PLUGIN=$CALCULATION_PLUGIN \
    EXTRA_PLUGINS=$EXTRA_PLUGINS

COPY src src
COPY srcext srcext
RUN python -m src.services.facescan.plugins.setup

# copy rest of the code
COPY tools tools
COPY sample_images sample_images

# run tests
ARG SKIP_TESTS
RUN if [ -z $SKIP_TESTS  ]; then pytest -m "not performance" /app/ml/src; fi

EXPOSE 3000

ENV UWSGI_PROCESSES=${UWSGI_PROCESSES:-2} \
    UWSGI_THREADS=1

ENTRYPOINT ["./entrypoint.sh"]
