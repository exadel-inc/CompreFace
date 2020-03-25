FROM jjanzic/docker-python3-opencv
WORKDIR /srv

## Copy sources
COPY src src
COPY srcext srcext
COPY requirements.txt .
COPY pytest.ini .
COPY pylama.ini .
COPY wait-for-it.sh .
COPY uwsgi.ini .

## Prepare environment
RUN chmod +x wait-for-it.sh
RUN python3 -m pip --no-cache-dir install -r requirements.txt
RUN python3 -m pip --no-cache-dir install -e srcext/insightface/python-package
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
RUN echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.0.list
RUN apt-get update
RUN apt-get install -y mongodb-org-tools mongodb-org-shell python-pymongo
RUN mkdir mongo_data
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0

## Run tests
ARG DO_RUN_TESTS
RUN if [ "$DO_RUN_TESTS" = true ] ; then \
    python3 -m pytest /srv/src && \
    python3 -m pylama --options /srv/pylama.ini /srv/src; \
    fi

## Runtime
RUN ./wait-for-it.sh mongo:27017
CMD ["uwsgi", "--ini", "uwsgi.ini"]
