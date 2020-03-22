.PHONY: build up down setup run stop default local unit i9n e2e lint
.DEFAULT_GOAL := default

build:
	docker-compose build ml

up:
	docker-compose up ml

down:
	docker-compose down ml

setup:
	python -m pip install -r ./e2e/requirements.txt
	python -m pip install -r ./ml/requirements.txt
	python -m pip install -e ./ml/src/services/facescan/backend/insightface/extlib/insightface/python-package

run: ml/run.pid
ml/run.pid:
	$(CURDIR)/ml/run.sh start

stop: ml/run.pid
	$(CURDIR)/ml/run.sh stop

default:
	docker-compose up --build --abort-on-container-exit e2e

local: unit i9n e2e lint

unit:
	python -m pytest -m "not integration" $(CURDIR)/ml/src

i9n:
	python -m pytest -m "integration" $(CURDIR)/ml/src

e2e: run
	$(CURDIR)/e2e/run-e2e-test.sh http://localhost:3000 \
		&& $(CURDIR)/ml/run.sh stop \
		|| ($(CURDIR)/ml/run.sh stop; exit 1)

lint:
	python -m pylama --options $(CURDIR)/ml/pylama.ini $(CURDIR)/ml/src
