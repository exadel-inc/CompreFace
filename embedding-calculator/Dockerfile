ARG GPU_IDX
ARG BASE_IMAGE=${GPU_IDX:+exadel/compreface-core-cuda:101-py37}
FROM ${BASE_IMAGE:-python:3.7-slim}

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
ARG DETECTION_MODEL
ARG CALCULATION_MODEL
ENV DETECTION_MODEL=${DETECTION_MODEL:-retinaface_r50_v1}
ENV CALCULATION_MODEL=${CALCULATION_MODEL:-arcface_r100_v1}
COPY srcext srcext
COPY pytest.ini .
COPY *.sh ./
RUN chmod +x *.sh
RUN ./prepare_scanners.sh

# install InsightFace packages
ARG INTEL_OPTIMIZATION
ARG GPU_IDX
ENV GPU_IDX=$GPU_IDX
ENV MXNET_MOD=${GPU_IDX:+cu101}${INTEL_OPTIMIZATION:+mkl}
ENV MXNET_LIB=mxnet${MXNET_MOD:+-$MXNET_MOD}
ENV MXNET_VER="<1.7"

RUN if [[ "$SCANNER" == "InsightFace" ]]; then \
      pip --no-cache-dir install "$MXNET_LIB$MXNET_VER" -e srcext/insightface/python-package; \
    fi

# copy rest of the code
COPY src src
COPY tools tools
COPY sample_images sample_images

# run tests
ARG SKIP_TESTS
RUN if [ -z $SKIP_TESTS  ]; then pytest -m "not performance" /app/ml/src; fi

EXPOSE 3000

COPY uwsgi.ini .
ENV UWSGI_PROCESSES=${UWSGI_PROCESSES:-2}
ENV UWSGI_THREADS=1
CMD ["uwsgi", "--ini", "uwsgi.ini"]
