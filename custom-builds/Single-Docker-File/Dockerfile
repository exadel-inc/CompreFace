ARG BASE_IMAGE=exadel/compreface-core:latest
ARG VERSION=latest

################# init images start ####################
FROM exadel/compreface-postgres-db:${VERSION} as postgres_db
FROM exadel/compreface-admin:${VERSION} as admin
FROM exadel/compreface-api:${VERSION} as api
FROM exadel/compreface-fe:${VERSION} as fe
################# init images end ####################

################# compreface-core start ####################
FROM ${BASE_IMAGE}

ENV UWSGI_PROCESSES=2
ENV UWSGI_THREADS=1
################# compreface-core end ####################

ENV TZ=America/Los_Angeles
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

################# compreface-postgres-db start ####################
ENV POSTGRES_USER=compreface
ENV POSTGRES_PASSWORD=M7yfTsBscdqvZs49
ENV POSTGRES_DB=frs
ENV PGDATA=/var/lib/postgresql/data

RUN apt-get update && apt-get install -y lsb-release
RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN apt-get update && apt-get install -y postgresql-13 \
    && rm -rf /var/lib/apt/lists/*

RUN rm /etc/postgresql/13/main/postgresql.conf
COPY custom-builds/Single-Docker-File/postgresql.conf /etc/postgresql/13/main/postgresql.conf
RUN mv /var/lib/postgresql/13/main $PGDATA

COPY --from=postgres_db /docker-entrypoint-initdb.d/initdb.sql /initdb.sql

USER postgres

RUN /etc/init.d/postgresql start && \
    psql --command "CREATE USER ${POSTGRES_USER} WITH SUPERUSER PASSWORD '${POSTGRES_PASSWORD}';" && \
    createdb -O ${POSTGRES_USER} ${POSTGRES_DB} && \
    psql -d ${POSTGRES_DB} -a -f /initdb.sql

RUN cp -r $PGDATA /var/lib/postgresql/default

USER root

################# compreface-postgres-db end ####################

################# compreface-admin start ####################

ENV POSTGRES_URL=jdbc:postgresql://localhost:5432/${POSTGRES_DB}
ENV SPRING_PROFILES_ACTIVE=dev
ENV ENABLE_EMAIL_SERVER=false
ENV ADMIN_JAVA_OPTS=-Xmx1g
ENV CRUD_PORT=8081
ENV PYTHON_URL=http://localhost:3000
ENV MAX_FILE_SIZE=5MB
ENV MAX_REQUEST_SIZE=10MB

RUN apt-get update && apt-get install -y openjdk-11-jre-headless \
    && rm -rf /var/lib/apt/lists/*

COPY --from=admin /home/app.jar /app/admin/app.jar
ARG APPERY_API_KEY
ENV APPERY_API_KEY ${APPERY_API_KEY}

################# compreface-admin end ####################

################# compreface-api start ####################

ENV API_JAVA_OPTS=-Xmx4g
ENV SAVE_IMAGES_TO_DB=true
ENV API_PORT=8080
ENV CONNECTION_TIMEOUT=10000
ENV READ_TIMEOUT=60000

COPY --from=api /home/app.jar /app/api/app.jar

################# compreface-api end ####################

################# compreface-fe start ####################

RUN apt-get update && apt-get install -y nginx \
    && rm -rf /var/lib/apt/lists/*
RUN adduser --system --no-create-home --shell /bin/false --group --disabled-login nginx

USER nginx

COPY --from=fe /usr/share/nginx/html /usr/share/nginx/html
COPY --from=fe /etc/nginx/ /etc/nginx/
COPY custom-builds/Single-Docker-File/nginx.conf /etc/nginx/conf.d/nginx.conf

USER root
################# compreface-fe end ####################

################# supervisord ####################
RUN apt-get update && apt-get install -y supervisor mc && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /var/log/supervisor
COPY custom-builds/Single-Docker-File/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY custom-builds/Single-Docker-File/startup.sh /startup.sh
RUN chmod +x /startup.sh

CMD ["/usr/bin/supervisord"]
