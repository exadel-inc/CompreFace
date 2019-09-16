FROM ubuntu:18.04

ENV DEBIAN_FRONTEND noninteractive

# Install some dependencies
RUN apt-get update && apt-get install -y \
		bc \
		build-essential \
		cmake \
		curl \
		g++ \
		gfortran \
		git \
		libffi-dev \
		libfreetype6-dev \
		libhdf5-dev \
		libjpeg-dev \
		liblcms2-dev \
		libopenblas-dev \
		liblapack-dev \
		libssl-dev \
		libwebp-dev \
		libzmq3-dev \
		nano \
		pkg-config \
		software-properties-common \
		unzip \
		vim \
		wget \
		zlib1g-dev \
		qt5-default \
		libvtk6-dev \
		zlib1g-dev \
		libjpeg-dev \
		libwebp-dev \
		libpng-dev \
		libtiff5-dev \
		libjpeg8-dev \
		libopenexr-dev \
		libgdal-dev \
		libdc1394-22-dev \
		libavcodec-dev \
		libavformat-dev \
		libswscale-dev \
		libtheora-dev \
		libvorbis-dev \
		libxvidcore-dev \
		libx264-dev \
		libblas-dev \
		yasm \
		libopencore-amrnb-dev \
		libopencore-amrwb-dev \
		libv4l-dev \
		libxine2-dev \
		libtbb-dev \
		libeigen3-dev \
		python-dev \
		python-tk \
		python-numpy \
		python3-dev \
		python3-tk \
		python3-numpy \
		ant \
		default-jdk \
		doxygen \
		mc \
		&& \
	apt-get clean && \
	apt-get autoremove && \
	rm -rf /var/lib/apt/lists/*

# Install OpenCV
RUN git clone --depth 1 https://github.com/opencv/opencv.git /root/opencv && \
	cd /root/opencv && \
	mkdir build && \
	cd build && \
	cmake .. && \
	make -j"$(nproc)"  && \
	make install && \
	ldconfig && \
	echo 'ln /dev/null /dev/raw1394' >> ~/.bashrc && \
    rm -rf /root/opencv

# Install pip
RUN curl -O https://bootstrap.pypa.io/get-pip.py && \
	python3 get-pip.py && \
	rm get-pip.py

# Install TensorFlow
RUN pip3 --no-cache-dir install -U tensorflow

# Install other useful Python packages using pip
RUN pip3 --no-cache-dir install \
		six \
		flask \
		scikit-learn \
		pymongo \
		imageio \
		numpy \
		scipy \
		matplotlib \
		pandas \
		sympy \
		nose \
		h5py \
        scikit-image \
		opencv-python \
		flasgger

RUN apt-get update && apt-get install -y mongodb-clients

# Expose Ports for TensorBoard (6006)
EXPOSE 6006
# Expose Flask port
EXPOSE 5000

# Copy sources
RUN mkdir /facerecognition
COPY facerecognition /facerecognition/facerecognition
COPY docker-entrypoint.sh /facerecognition/docker-entrypoint.sh
COPY wait-for-it.sh /facerecognition/wait-for-it.sh
COPY dump.archive /facerecognition/dump.archive
RUN chmod +x /facerecognition/docker-entrypoint.sh
RUN chmod +x /facerecognition/wait-for-it.sh
RUN mkdir /facerecognition/mongo_data

WORKDIR /facerecognition

ENTRYPOINT ["/facerecognition/docker-entrypoint.sh"]