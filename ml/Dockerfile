FROM jjanzic/docker-python3-opencv
WORKDIR /srv

## Copy sources
COPY src src
COPY requirements.txt .
COPY pytest.ini .
COPY pylama.ini .
COPY docker-entrypoint.sh .
COPY wait-for-it.sh .
COPY uwsgi.ini .

## Prepare environment
RUN chmod +x docker-entrypoint.sh
RUN chmod +x wait-for-it.sh
RUN pip3 --no-cache-dir install -r requirements.txt
RUN pip3 --no-cache-dir install -e src/services/facescan/backend/insightface/extlib/insightface/python-package
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
RUN echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.0.list
RUN apt-get update
RUN apt-get install -y mongodb-org-tools mongodb-org-shell python-pymongo
RUN mkdir mongo_data

## Run tests
ARG DO_TESTS=false
RUN if [ "$DO_TESTS" = true ] ; then \
        docker exec ml python3 -m pytest /srv/src && \
        docker exec ml python3 -m pylama --options /srv/pylama.ini /srv/src; \
    fi


## Entrypoint
ENTRYPOINT ["/srv/docker-entrypoint.sh"]
