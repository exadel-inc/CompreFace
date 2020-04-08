SHELL := /bin/bash
.PHONY: $(MAKECMDGOALS)
.EXPORT_ALL_VARIABLES:
.DEFAULT_GOAL := default
FLASK_ENV ?= development
MONGODB_HOST ?= localhost
MONGODB_PORT ?= 27017

#####################################
##### MAIN TEST
#####################################

default: test/unit test/lint test

test:
	docker-compose up --build --abort-on-container-exit

#####################################
##### RUNNING IN DOCKER
#####################################

build:
	docker-compose build ml

up:
	docker-compose up ml

down:
	docker-compose down

#####################################
##### RUNNING IN LOCAL ENVIRONMENT
#####################################

setup:
	chmod +x ml/run.sh e2e/run-e2e-test.sh ml/tools/test_oom.sh
	python -m pip install -r ml/requirements.txt
	python -m pip install -e ml/srcext/insightface/python-package

start:
	ml/run.sh start

stop:
	ml/run.sh stop

#####################################
##### TESTING IN LOCAL ENVIRONMENT
#####################################

test/local: test/unit test/lint test/i9n

test/unit:
	python -m pytest -m "not integration" ml/src

test/lint:
	python -m pylama --options ml/pylama.ini ml/src

test/i9n:
	python -m pytest -m integration $(CURDIR)/ml/src

test/e2e: e2e/local

#####################################
##### E2E TESTING
#####################################

e2e:
	e2e/run-e2e-test.sh

e2e/local: start
	timeout 10s bash -c "until [ -f $(CURDIR)/ml/run.pid ]; do sleep 1; done"
	sleep 5s
	test -f $(CURDIR)/ml/run.pid
	$(MAKE) e2e && ml/run.sh stop || (ml/run.sh stop; exit 1)

#####################################
#####  DEV SCRIPTS
#####################################

tool/oom:
	ml/tools/test_oom.sh $(CURDIR)/ml/sample_images

tool/opt:
	python -m ml.tools.optimize_face_det_constants

tool/scan:
	python -m ml.tools.scan_faces

tool/err:
	python -m ml.tools.calculate_errors

#####################################
##### HELPERS
#####################################

db:
	docker-compose up -d mongodb

PORT:
	@echo $$(while true; do port=$$(( RANDOM % 30000 + 30000 )); echo -ne "\035" | telnet 127.0.0.1 $$port > /dev/null 2>&1; [ $$? -eq 1 ] && echo "$$port" && exit 0; done )

TIMESTAMP:
	@echo $$(date +'%Y-%m-%d-%H-%M-%S')

#####################################
##### MISC
#####################################

stats:
	(which tokei || conda install -y -c conda-forge tokei) && \
	tokei --exclude srcext/
