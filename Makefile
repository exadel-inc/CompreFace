.PHONY: build up down setup start stop docker local unit i9n e2e lint all oom extended
.DEFAULT_GOAL := docker
PORT = 3000
ID =

build:
	PORT=$(PORT) ID=$(ID) docker-compose build ml

up:
	PORT=$(PORT) ID=$(ID) docker-compose up ml

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
	DO_RUN_TESTS=true PORT=$(PORT) ID=$(ID) docker-compose up --build --abort-on-container-exit

local: unit i9n e2e lint

unit:
	python -m pytest -m "not integration" $(CURDIR)/ml/src

i9n:
	python -m pytest -m integration $(CURDIR)/ml/src

e2e: start
	$(CURDIR)/e2e/run-e2e-test.sh http://localhost:$(PORT) \
		&& $(CURDIR)/ml/run.sh stop \
		|| ($(CURDIR)/ml/run.sh stop; exit 1)

lint:
	python -m pylama --options $(CURDIR)/ml/pylama.ini $(CURDIR)/ml/src

all: local docker

oom:
	$(CURDIR)/tools/test_oom/run.sh

extended: all oom

x:
	print $(abspath $(lastword $(MAKEFILE_LIST)))
