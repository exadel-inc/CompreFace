from jjanzic/docker-python3-opencv

# Copy sources
RUN mkdir /facerecognition
COPY facerecognition /facerecognition/facerecognition
COPY docker-entrypoint.sh /facerecognition/docker-entrypoint.sh
COPY wait-for-it.sh /facerecognition/wait-for-it.sh
COPY dump.archive /facerecognition/dump.archive
COPY uwsgi.ini /facerecognition/uwsgi.ini
COPY requirements.txt /facerecognition/requirements.txt
RUN chmod +x /facerecognition/docker-entrypoint.sh
RUN chmod +x /facerecognition/wait-for-it.sh
RUN mkdir /facerecognition/mongo_data


# Install dependencies
RUN apt-get update && apt-get install -y \
    mongo-tools \
    mongodb-clients \
    nginx
COPY nginx.conf /etc/nginx

RUN pip3 --no-cache-dir install -r /facerecognition/requirements.txt


# Expose API port
EXPOSE 5000
# Expose TensorBoard port
EXPOSE 6006


WORKDIR /facerecognition
ENTRYPOINT ["/facerecognition/docker-entrypoint.sh"]
