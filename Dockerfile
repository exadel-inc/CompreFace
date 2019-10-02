from jjanzic/docker-python3-opencv

RUN apt-get update && apt-get install -y mongo-tools

# Install other useful Python packages using pip
RUN pip3 --no-cache-dir install \
		tensorflow==1.13.1 \
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
		flasgger \
		sqlalchemy

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