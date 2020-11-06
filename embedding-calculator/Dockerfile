# Changes to this file have to be applied to DevOps Dockerfile as well, e.g. https://git.exadel.com/exadel-face-recognition-service/frs-devops/blob/master/Docker/APP-GENERIC-PYTHON-dev2/Dockerfile
FROM python:3.7-slim-buster
RUN apt-get update && apt-get install -y build-essential cmake git wget unzip \
        curl yasm pkg-config libswscale-dev libtbb2 libtbb-dev libjpeg-dev \
        libpng-dev libtiff-dev libavformat-dev libpq-dev libfreeimage3 \
    && rm -rf /var/lib/apt/lists/*

# install common python packages
SHELL ["/bin/bash", "-c"]
WORKDIR /app/ml
COPY requirements.txt .
RUN pip --no-cache-dir install -r requirements.txt

ARG SCANNER=Facenet2018
ENV SCANNER=$SCANNER
ARG BE_VERSION
ARG APP_VERSION_STRING
ENV BE_VERSION=$BE_VERSION
ENV APP_VERSION_STRING=$APP_VERSION_STRING
ENV HOME=/app/ml
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0

# download ML models
ARG DETECTION_MODEL="retinaface_r50_v1"
ARG CALCULATION_MODEL="arcface_r100_v1"
ENV DETECTION_MODEL=$DETECTION_MODEL CALCULATION_MODEL=$CALCULATION_MODEL
COPY download_models.sh .
RUN ./download_models.sh

# install InsightFace packages
ARG MXNET
ARG GPU_ID
ENV GPU_ID=$GPU_ID
COPY srcext srcext
RUN if [[ "$SCANNER" == "InsightFace" ]]; then \
      pip --no-cache-dir install ${MXNET:-mxnet==1.6.0} -e srcext/insightface/python-package; \
    fi

# <different-from-jenkins>
COPY sample_images sample_images
COPY src src
COPY tools tools
COPY *.ini ./
# </different-from-jenkins>

# run tests
ARG SKIP_TESTS
RUN if [ -z $SKIP_TESTS  ]; then pytest -m "not performance" /app/ml/src; fi

EXPOSE 3000
CMD ["uwsgi", "--ini", "uwsgi.ini"]
