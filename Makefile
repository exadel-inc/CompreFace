ML_PORT = 3000
ID =

.PHONY: build up down setup start stop docker local unit i9n e2e lint all oom extended
local: unit i9n e2e lint
all: local docker
extended: all oom
.DEFAULT_GOAL := docker

build:
	ML_PORT=$(ML_PORT) ID=$(ID) COMPOSE_PROJECT_NAME=frs-core \
		docker-compose build ml

up:
	ML_PORT=$(ML_PORT) ID=$(ID) COMPOSE_PROJECT_NAME=frs-core \
		docker-compose up ml

down:
	docker-compose down

setup:
	python -m pip install -r ./ml/requirements.txt
	python -m pip install -e ./ml/srcext/insightface/python-package
	chmod +x ml/run.sh e2e/run-e2e-test.sh

start:
	$(CURDIR)/ml/run.sh start $(PORT)

stop:
	$(CURDIR)/ml/run.sh stop

docker:
	DO_RUN_TESTS=true ML_PORT=$(ML_PORT) ID=$(ID) COMPOSE_PROJECT_NAME=frs-core \
		docker-compose up --build --abort-on-container-exit

unit:
	python -m pytest -m "not integration" $(CURDIR)/ml/src

i9n:
	python -m pytest -m integration $(CURDIR)/ml/src

e2e: start
	$(CURDIR)/e2e/run-e2e-test.sh http://localhost:$(ML_PORT) \
		&& ML_PORT=$(ML_PORT) $(CURDIR)/ml/run.sh stop \
		|| ($(CURDIR)/ml/run.sh stop; exit 1)

lint:
	python -m pylama --options $(CURDIR)/ml/pylama.ini $(CURDIR)/ml/src

oom:
	ID=$(ID) $(CURDIR)/tools/test_oom/run.sh $(CURDIR)/ml/sample_images
