# Changes to this file have to be applied to DevOps Dockerfile as well, e.g. https://git.exadel.com/exadel-face-recognition-service/frs-devops/blob/master/Docker/APP-GENERIC-PYTHON-dev2/Dockerfile
FROM jjanzic/docker-python3-opencv

# <different-from-jenkins>
WORKDIR /app/ml
COPY sample_images sample_images
COPY src src
COPY srcext srcext
COPY pylama.ini .
COPY pytest.ini .
COPY requirements.txt .
COPY uwsgi.ini .
# </different-from-jenkins>

ENV HOME=/app/ml
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
ENV PYTHONUNBUFFERED=0
ENV JOBLIB_MULTIPROCESSING=0

RUN python3 -m pip --no-cache-dir install -r requirements.txt \
&& imageio_download_bin freeimage \
&& python3 -m pip --no-cache-dir install -e srcext/insightface/python-package \
&& apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4 \
&& echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.0.list \
&& apt-get update \
&& apt-get install -y mongodb-org-tools mongodb-org-shell python-pymongo \
&& mkdir mongo_data

ARG SKIP_TESTS
RUN if [ "$SKIP_TESTS" != true ]; then python -m pytest /app/ml/src; fi
RUN if [ "$SKIP_TESTS" != true ]; then python -m pylama --options /app/ml/pylama.ini /app/ml/src; fi

RUN SCANNER=Facenet2018 IMG_NAMES=000_5.jpg SHOW_IMG=false python -m src.services.facescan.run
# (TODO EFRS-435: add this line ->) RUN SCANNER=InsightFace IMG_NAMES=000_5.jpg SHOW_IMG=false python -m src.services.facescan.run

EXPOSE 3000
CMD ["uwsgi", "--ini", "uwsgi.ini"]
