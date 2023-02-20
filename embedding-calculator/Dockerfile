ARG BASE_IMAGE
FROM ${BASE_IMAGE:-python:3.7-slim}

RUN apt-get update && apt-get install -y build-essential cmake git wget unzip \
        curl yasm pkg-config libswscale-dev libtbb2 libtbb-dev libjpeg-dev \
        libpng-dev libtiff-dev libavformat-dev libpq-dev libfreeimage3 python3-opencv \
        libaec-dev libblosc-dev libbrotli-dev libbz2-dev libgif-dev libopenjp2-7-dev \
        liblcms2-dev libcharls-dev libjxr-dev liblz4-dev libcfitsio-dev libsnappy-dev \
        libwebp-dev libzopfli-dev libzstd-dev \
    && rm -rf /var/lib/apt/lists/*

# Dependencies for imagecodecs
WORKDIR /tmp

# brunsli
RUN git clone --depth=1 --shallow-submodules --recursive -b v0.1 https://github.com/google/brunsli && \
	cd brunsli && \
	cmake -DCMAKE_BUILD_TYPE=Release . && \
	make -j$(nproc) install && \
	rm -rf /tmp/brunsli

# libjxl
RUN git clone --depth=1 --shallow-submodules --recursive -b v0.7.0 https://github.com/libjxl/libjxl && \
	cd libjxl && \
	cmake -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTING=OFF . && \
	make -j$(nproc) install && \
	rm -rf /tmp/libjxl

# zfp
RUN git clone --depth=1 -b 0.5.5 https://github.com/LLNL/zfp && \
	cd zfp && \
	mkdir build && \
	cd build && \
	cmake -DCMAKE_BUILD_TYPE=Release .. && \
	make -j$(nproc) install && \
	rm -rf /tmp/zfp
# End imagecodecs dependencies

# install common python packages
SHELL ["/bin/bash", "-c"]
WORKDIR /app/ml
COPY requirements.txt .
# Ensure numpy is installed first as imagecodecs doesn't declare dependencies correctly until 2022.9.26,
# which is not compatible with Python 3.7
RUN pip --no-cache-dir install $(grep ^numpy requirements.txt)
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

# download ML models
ARG INTEL_OPTIMIZATION=false
ARG GPU_IDX=-1
ENV GPU_IDX=$GPU_IDX INTEL_OPTIMIZATION=$INTEL_OPTIMIZATION
ARG FACE_DETECTION_PLUGIN="facenet.FaceDetector"
ARG CALCULATION_PLUGIN="facenet.Calculator"
ARG EXTRA_PLUGINS="facenet.LandmarksDetector,agegender.AgeDetector,agegender.GenderDetector,facenet.facemask.MaskDetector,facenet.PoseEstimator"
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
CMD ["uwsgi", "--ini", "uwsgi.ini"]