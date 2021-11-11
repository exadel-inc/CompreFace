################# build compreface-api and compreface-admin start ####################
FROM maven:3.6.3-jdk-11-slim as build
ARG ND4J_CLASSIFIER
WORKDIR /workspace/compreface
LABEL intermidiate_frs=true
COPY java/pom.xml .
COPY java/api/pom.xml api/
COPY java/admin/pom.xml admin/
COPY java/common/pom.xml common/
RUN mvn -B clean install -DskipTests -Dcheckstyle.skip -Dasciidoctor.skip -Djacoco.skip -Dmaven.gitcommitid.skip -Dspring-boot.repackage.skip -Dmaven.exec.skip=true -Dmaven.install.skip -Dmaven.resources.skip
COPY java/api api
COPY java/admin admin
COPY java/common common
RUN mvn package -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true -Dnd4j.classifier=$ND4J_CLASSIFIER
################# build compreface-api and compreface-admin end ####################

ARG BASE_IMAGE
FROM ${BASE_IMAGE:-python:3.7-slim}

################# compreface-core start ####################
RUN apt-get update && apt-get install -y build-essential cmake git wget unzip \
        curl yasm pkg-config libswscale-dev libtbb2 libtbb-dev libjpeg-dev \
        libpng-dev libtiff-dev libavformat-dev libpq-dev libfreeimage3 mc \
    && rm -rf /var/lib/apt/lists/*

# install common python packages
SHELL ["/bin/bash", "-c"]
WORKDIR /app/ml
COPY embedding-calculator/requirements.txt .
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
ARG EXTRA_PLUGINS="facenet.LandmarksDetector,agegender.AgeDetector,agegender.GenderDetector,facenet.facemask.MaskDetector"
ENV FACE_DETECTION_PLUGIN=$FACE_DETECTION_PLUGIN CALCULATION_PLUGIN=$CALCULATION_PLUGIN \
    EXTRA_PLUGINS=$EXTRA_PLUGINS
COPY embedding-calculator/src src
COPY embedding-calculator/srcext srcext
RUN python -m src.services.facescan.plugins.setup

# copy rest of the code
COPY embedding-calculator/tools tools
COPY embedding-calculator/sample_images sample_images

# run tests
ARG SKIP_TESTS
COPY embedding-calculator/pytest.ini .
RUN if [ -z $SKIP_TESTS  ]; then pytest -m "not performance" /app/ml/src; fi

ENV ML_PORT=3000
EXPOSE 3000

COPY embedding-calculator/uwsgi.ini .
ENV UWSGI_PROCESSES=${UWSGI_PROCESSES:-2}
ENV UWSGI_THREADS=1
################# compreface-core end ####################

################# compreface-postgres-db start ####################
ENV POSTGRES_USER=compreface
ENV POSTGRES_PASSWORD=M7yfTsBscdqvZs49
ENV POSTGRES_DB=frs

RUN apt-get update && apt-get install -y postgresql-13 \
    && rm -rf /var/lib/apt/lists/*

USER postgres

RUN /etc/init.d/postgresql start && \
    psql --command "CREATE USER ${POSTGRES_USER} WITH SUPERUSER PASSWORD '${POSTGRES_PASSWORD}';" &&\
    createdb -O ${POSTGRES_USER} ${POSTGRES_DB}

USER root

################# compreface-postgres-db end ####################

################# compreface-admin start ####################

ENV POSTGRES_URL=jdbc:postgresql://localhost:5432/${POSTGRES_DB}
ENV SPRING_PROFILES_ACTIVE=dev
ENV ENABLE_EMAIL_SERVER=false
ENV ADMIN_JAVA_OPTS=-Xmx1g

RUN apt-get update && apt-get install -y openjdk-11-jre-headless \
    && rm -rf /var/lib/apt/lists/*

ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/admin/target/*.jar /java/admin/app.jar
ARG APPERY_API_KEY
ENV APPERY_API_KEY ${APPERY_API_KEY}

################# compreface-admin end ####################

################# supervisord ####################
RUN apt-get update && apt-get install -y supervisor && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /var/log/supervisor
COPY custom-builds/Single-Docker-File/FaceNet/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

CMD ["/usr/bin/supervisord"]
