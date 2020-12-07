# Changes to this file have to be applied to DevOps Dockerfile as well, e.g. https://git.exadel.com/exadel-face-recognition-service/frs-devops/blob/master/Docker/APP-GENERIC-PYTHON-dev2/Dockerfile
FROM jjanzic/docker-python3-opencv@sha256:d4182ab5f84d40057a96ad43043855cc6b6e695b30f1c1a4867b1eb8930e6d83

# <different-from-jenkins>
WORKDIR /app/ml
COPY sample_images sample_images
COPY src src
COPY srcext srcext
COPY tools tools
COPY pylama.ini .
COPY pytest.ini .
COPY requirements.txt .
COPY uwsgi.ini .
COPY dockerfile.sh .
# </different-from-jenkins>

ARG BE_VERSION
ENV BE_VERSION=$BE_VERSION
ARG APP_VERSION_STRING
ENV APP_VERSION_STRING=$APP_VERSION_STRING
ARG SKIP_TESTS
ARG SCANNER=Facenet2018
ENV HOME=/app/ml
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0

RUN chmod +x dockerfile.sh
RUN ./dockerfile.sh

EXPOSE 3000
CMD ["uwsgi", "--ini", "uwsgi.ini"]
