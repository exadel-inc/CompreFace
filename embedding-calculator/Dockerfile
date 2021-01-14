ARG GPU_IDX
# ARG BASE_IMAGE=${GPU_IDX:+exadel/compreface-core-cuda:100-py37}
# FROM ${BASE_IMAGE:-python:3.7-slim}
FROM nvcr.io/nvidia/tensorrt:20.12-py3

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt-get update && apt-get install -y libgl1-mesa-glx protobuf-compiler libprotoc-dev libb64-0d
ENV PIP_INSTALL="python -m pip --no-cache-dir install --upgrade"
RUN $PIP_INSTALL mxnet insightface fastapi uvicorn ujson numba aiofiles
RUN $PIP_INSTALL onnx==1.7.0
RUN $PIP_INSTALL onnx-simplifier
RUN $PIP_INSTALL nvidia-pyindex && pip install tritonclient[http,grpc]

# Workaround for tensorrt:20.12-py3 image, which has broken support for BatchNorm layers
RUN bash /opt/tensorrt/install_opensource.sh -b master
# For some reason symlink libnvonnxparser.so.7 won't update to newly compiled version if old binary exist
RUN rm /usr/lib/x86_64-linux-gnu/libnvonnxparser.so.7.2.2



# RUN apt-get update && apt-get install -y build-essential cmake git wget unzip \
#         curl yasm pkg-config libswscale-dev libtbb2 libtbb-dev libjpeg-dev \
#         libpng-dev libtiff-dev libavformat-dev libpq-dev libfreeimage3 \
#     && rm -rf /var/lib/apt/lists/*

# install common python packages
SHELL ["/bin/bash", "-c"]
WORKDIR /app/ml
COPY requirements.txt .
RUN pip --no-cache-dir install -r requirements.txt

ARG BE_VERSION
ARG APP_VERSION_STRING
ENV BE_VERSION=$BE_VERSION
ENV APP_VERSION_STRING=$APP_VERSION_STRING
ENV HOME=/app/ml
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0

# install tvm
ENV DEBIAN_FRONTEND=noninteractive
COPY *.sh ./
RUN chmod +x *.sh
RUN ./install_tvm.sh

# download ML models
ARG INTEL_OPTIMIZATION=false
ARG GPU_IDX=-1
ENV GPU_IDX=$GPU_IDX INTEL_OPTIMIZATION=$INTEL_OPTIMIZATION
ARG FACE_DETECTION_PLUGIN="facenet.FaceDetector"
ARG CALCULATION_PLUGIN="facenet.Calculator"
ARG EXTRA_PLUGINS="facenet.LandmarksDetector"
ENV FACE_DETECTION_PLUGIN=$FACE_DETECTION_PLUGIN CALCULATION_PLUGIN=$CALCULATION_PLUGIN \
    EXTRA_PLUGINS=$EXTRA_PLUGINS
COPY src src
COPY srcext srcext
RUN python -m src.services.facescan.plugins.setup

# copy rest of the code
COPY tools tools
COPY sample_images sample_images

# run tests
ARG SKIP_TESTS
COPY pytest.ini .
RUN if [ -z $SKIP_TESTS  ]; then pytest -m "not performance" /app/ml/src; fi

EXPOSE 3000

COPY uwsgi.ini .
ENV UWSGI_PROCESSES=${UWSGI_PROCESSES:-2}
ENV UWSGI_THREADS=1

COPY srcext/api_trt .

CMD ["uwsgi", "--ini", "uwsgi.ini"]
