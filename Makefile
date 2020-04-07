.PHONY: default setup start stop build up down local unit i9n e2e _start_before_e2e e2e_remote lint docker oom scan err stats opt db
.DEFAULT_GOAL := default
default: lint unit docker
RANDOM_PORT := $$((20000 + RANDOM % 9999))
RANDOM_UUID := $$(uuidgen)

ML_PORT ?= 3000
ML_URL ?= http://localhost:$(ML_PORT)
MONGODB_HOST ?= localhost
FLASK_ENV ?= development

###################
### RUNNING LOCAL
###################

setup:
	chmod +x $(CURDIR)/ml/run.sh $(CURDIR)/e2e/run-e2e-test.sh $(CURDIR)/ml/tools/test_oom.sh
	python -m pip install -r $(CURDIR)/ml/requirements.txt
	python -m pip install -e $(CURDIR)/ml/srcext/insightface/python-

start:
	ML_PORT=$(ML_PORT) \
	MONGODB_URI=$(MONGODB_URI) \
	MONGODB_HOST=$(MONGODB_HOST) \
	MONGODB_PORT=$(MONGODB_PORT) \
	MONGODB_DBNAME=efrs_tmp_db$(ID) \
	IMG_LENGTH_LIMIT=$(IMG_LENGTH_LIMIT) \
	FLASK_ENV=$(FLASK_ENV) \
	$(CURDIR)/ml/run.sh start

stop:
	$(CURDIR)/ml/run.sh stop

####################
### RUNNING DOCKER
####################

build:
	ML_PORT=$(ML_PORT) \
	MONGODB_PORT=$(MONGODB_PORT) \
	ID=$(ID) \
	IMG_LENGTH_LIMIT=$(IMG_LENGTH_LIMIT) \
	COMPOSE_PROJECT_NAME=frs-core \
	docker-compose build ml

up:
	ML_PORT=$(ML_PORT) \
	MONGODB_PORT=$(MONGODB_PORT) \
	ID=$(ID) \
	IMG_LENGTH_LIMIT=$(IMG_LENGTH_LIMIT) \
	COMPOSE_PROJECT_NAME=frs-core \
	docker-compose up ml

down:
	ML_PORT=$(ML_PORT) \
	MONGODB_PORT=$(MONGODB_PORT) \
	ID=$(ID) \
	IMG_LENGTH_LIMIT=$(IMG_LENGTH_LIMIT) \
	COMPOSE_PROJECT_NAME=frs-core \
	docker-compose down

#################
### TESTS LOCAL
local: unit i9n e2e lint
#################

unit:
	python -m pytest -m "not integration" $(CURDIR)/ml/src

i9n:
	python -m pytest -m integration $(CURDIR)/ml/src

e2e: _start_before_e2e e2e_remote
_start_before_e2e: start
	timeout 10s bash -c "until [ -f $(CURDIR)/ml/run.pid ]; do sleep 1; done"
	sleep 5s
	test -f $(CURDIR)/ml/run.pid

_e2e_remote:
	$(MAKE) e2e_remote ML_URL=http://qa.frs.exadel.by:3000 DROP_DB=false API_KEY=$(RANDOM_UUID)
e2e_remote:
	ML_URL=$(ML_URL) \
	API_KEY=$(API_KEY) \
	DROP_DB=$(DROP_DB) \
	MONGODB_URI=$(MONGODB_URI) \
	MONGODB_HOST=$(MONGODB_HOST) \
	MONGODB_PORT=$(MONGODB_PORT) \
	MONGODB_DBNAME=efrs_tmp_db$(ID) \
	$(CURDIR)/e2e/run-e2e-test.sh \
		&& $(CURDIR)/ml/run.sh stop \
		|| ($(CURDIR)/ml/run.sh stop; exit 1)

lint:
	python -m pylama --options $(CURDIR)/ml/pylama.ini $(CURDIR)/ml/src

##################
### TESTS DOCKER
##################

docker:
	ML_PORT=$(ML_PORT) \
	MONGODB_URI=$(MONGODB_URI) \
	MONGODB_PORT=$(MONGODB_PORT) \
	MONGODB_DBNAME=efrs_tmp_db \
	ID=$(ID) \
	COMPOSE_PROJECT_NAME=frs-core \
	IMG_LENGTH_LIMIT=$(IMG_LENGTH_LIMIT) \
	DO_RUN_TESTS=true \
	docker-compose up --build --abort-on-container-exit

#####################
### DEVELOPER TOOLS
#####################

stats: stats_setup.touch
	tokei --exclude srcext/
stats_setup.touch:
	conda install -c conda-forge tokei && touch $(CURDIR)/stats_setup.touch

db:
	[ ! -z "$(MONGODB_PORT)" ] && \
	docker run -p="$(MONGODB_PORT):27017" --name mongodb$(ID) mongo:4.0.4-xenial

scan:
	SCANNER=$(SCANNER) \
	IMG_NAME=$(IMG_NAME) \
	SHOW_IMG=$(SHOW_IMG) \
	python -m ml.tools.scan_faces

err:
	SCANNER=$(SCANNERS) \
	SHOW_IMG=$(SHOW_IMG) \
	python -m ml.tools.calculate_errors

opt:
	python -m ml.tools.optimize_face_det_constants

oom:
	ID=$(ID) \
	SCANNERS=$(SCANNERS) \
	IMG_NAMES=$(IMG_NAMES) \
	MEM_LIMITS=$(MEM_LIMITS) \
	IMG_LENGTH_LIMITS=$(IMG_LENGTH_LIMITS) \
	SHOW_OUTPUT=$(SHOW_OUTPUT) \
	$(CURDIR)/ml/tools/test_oom.sh $(CURDIR)/ml/sample_images

up_oom:
	[ ! -z "$(MEM_LIMIT)" ] && \
	docker run --network="host" -e MONGODB_HOST=$(MONGODB_HOST) --memory=$(MEM_LIMIT) --memory-swap=$(MEM_LIMIT) "frs-core_ml${ID}" uwsgi --ini uwsgi.ini
