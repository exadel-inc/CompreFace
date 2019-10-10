FROM jjanzic/docker-python3-opencv

# Variables
ARG DIR=/srv

# Copy sources
RUN mkdir -p $DIR
COPY facerecognition $DIR/facerecognition
COPY docker-entrypoint.sh $DIR/docker-entrypoint.sh
COPY wait-for-it.sh $DIR/wait-for-it.sh
COPY dump.archive $DIR/dump.archive
COPY uwsgi.ini $DIR/uwsgi.ini
COPY requirements.txt $DIR/requirements.txt
RUN chmod +x $DIR/docker-entrypoint.sh
RUN chmod +x $DIR/wait-for-it.sh
RUN mkdir $DIR/mongo_data


# Install dependencies
RUN apt-get update && apt-get install -y \
    mongo-tools \
    mongodb-clients \
    nginx
COPY nginx.conf /etc/nginx

RUN pip3 --no-cache-dir install -r $DIR/requirements.txt


# Expose API port
EXPOSE 5000
# Expose TensorBoard port
EXPOSE 6006


WORKDIR $DIR

RUN ln -s $DIR/docker-entrypoint.sh /var/tmp/docker-entrypoint.sh
ENTRYPOINT ["/var/tmp/docker-entrypoint.sh"]
