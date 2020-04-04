ML_HOST ?=
ML_PORT ?=
MONGO_HOST ?= localhost
MONGO_PORT ?=
ID ?=

.PHONY: build up down setup start stop docker local unit i9n e2e lint all oom extended
local: unit i9n e2e lint
all: local docker
extended: all oom
.DEFAULT_GOAL := docker

build:
	ML_PORT=$(ML_PORT) \
	MONGO_PORT=$(MONGO_PORT) \
	ID=$(ID) \
	COMPOSE_PROJECT_NAME=frs-core \
	docker-compose build ml

up:
	ML_PORT=$(ML_PORT) \
	MONGO_PORT=$(MONGO_PORT) \
	ID=$(ID) \
	COMPOSE_PROJECT_NAME=frs-core \
	docker-compose up ml

down:
	docker-compose down

setup:
	python -m pip install -r ./ml/requirements.txt
	python -m pip install -e ./ml/srcext/insightface/python-package
	chmod +x ml/run.sh e2e/run-e2e-test.sh

start:
	ML_PORT=$(ML_PORT) \
	MONGO_HOST=$(MONGO_HOST) \
	MONGO_PORT=$(MONGO_PORT) \
	MONGO_DBNAME=efrs_tmp_db$(ID) \
	$(CURDIR)/ml/run.sh start

stop:
	$(CURDIR)/ml/run.sh stop

docker:
	ML_PORT=$(ML_PORT) \
	MONGO_PORT=$(MONGO_PORT) \
	ID=$(ID) \
	COMPOSE_PROJECT_NAME=frs-core \
	DO_RUN_TESTS=true \
	MONGO_DBNAME=efrs_tmp_db \
	docker-compose up --build --abort-on-container-exit

unit:
	python -m pytest -m "not integration" $(CURDIR)/ml/src

i9n:
	python -m pytest -m integration $(CURDIR)/ml/src

e2e: start
	ML_URL=http://localhost:$(ML_PORT) \
	MONGO_HOST=$(MONGO_HOST) \
	MONGO_PORT=$(MONGO_PORT) \
	MONGO_DBNAME=efrs_tmp_db$(ID) \
	$(CURDIR)/e2e/run-e2e-test.sh \
		&& $(CURDIR)/ml/run.sh stop \
		|| ($(CURDIR)/ml/run.sh stop; exit 1)

lint:
	python -m pylama --options $(CURDIR)/ml/pylama.ini $(CURDIR)/ml/src

oom:
	ID=$(ID) \
	$(CURDIR)/tools/test_oom/run.sh $(CURDIR)/ml/sample_images
