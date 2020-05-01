SHELL := /bin/bash
.PHONY: default test build up down down/all setup start stop test/local test/unit test/lint test/i9n test/e2e e2e e2e/local e2e/extended e2e/remote e2e/dev e2e/qa demo scan optimize crash COMPOSE_PROJECT_NAME PORT API_KEY MONGODB_DBNAME db stats status/dev status/qa DEV_ML_URL QA_ML_URL benchmark/e2e benchmark/detection
.EXPORT_ALL_VARIABLES:
.DEFAULT_GOAL := default
DEV_ML_URL := http://10.130.66.129:3000
QA_ML_URL := http://10.130.66.141:3000
FLASK_ENV ?= development
ML_PORT ?= 3000
MONGODB_HOST ?= localhost
MONGODB_PORT ?= 27117
MONGODB_DBNAME ?= efrs_tmp_db
COMPOSE_PROJECT_NAME ?= frs-core
API_KEY ?= test-api-key
SKIP_TESTS ?= true
FORCE_FAIL_E2E_TESTS ?= false

#####################################
##### MAIN TESTS
#####################################

# Run main tests, with faster tests run first
default: test/unit test/lint test

# Run main tests
test: MEM_LIMIT = 4g
test: SKIP_TESTS = false
test: FLASK_ENV = production
test:
	docker-compose up --build --abort-on-container-exit

#####################################
##### RUNNING IN DOCKER
#####################################

# Build app's container
build:
	docker-compose build ml

# Run built app's container
up:
	docker-compose up ml

# Stop running containers
down:
	docker-compose down

# Stop all running containers
down/all:
	docker stop $$(docker ps -a -q)

#####################################
##### RUNNING IN LOCAL ENVIRONMENT
#####################################

# Install dependencies and prepare environment
setup: ml/tools/facescan/tmp
	mkdir -p tmp
	chmod +x ml/run.sh e2e/run-e2e-test.sh ml/tools/crash-lab.sh
	python -m pip install -r ml/requirements.txt -e ml/srcext/insightface/python-package
	imageio_download_bin freeimage

# Run application
start: db
	ml/run.sh start

# Stop application
stop:
	ml/run.sh stop

#####################################
##### TESTING IN LOCAL ENVIRONMENT
#####################################

# Run local tests
test/local: test/unit test/lint test/i9n

# Run unit tests
test/unit:
	python -m pytest -m "not integration" ml/src ml/tools

# Run lint checks
test/lint:
	python -m pylama --options ml/pylama.ini ml/src ml/tools

# Run integration tests
test/i9n:
	python -m pytest -m integration ml/src ml/tools

# Run E2E tests (also starts db and application automatically)
test/e2e: e2e/local

#####################################
##### E2E TESTING
#####################################

# Runs E2E tests against an already running application
e2e: API_KEY=$(shell $(MAKE) API_KEY)
e2e:
	e2e/run-e2e-test.sh

# Runs E2E after automatically starting db and application automatically
e2e/local: start
	timeout 10s bash -c "until [ -f $(CURDIR)/ml/$(COMPOSE_PROJECT_NAME).pid ]; do sleep 1; done"
	sleep 5s
	test -f $(CURDIR)/ml/$(COMPOSE_PROJECT_NAME).pid
	$(MAKE) e2e && ml/run.sh stop || (ml/run.sh stop; exit 1)

# Runs E2E and also checks if given host is able to handle scanning all images
e2e/extended: SAVE_IMG=false
e2e/extended: LOGGING_LEVEL_NAME=info
e2e/extended: IMG_NAMES={test_images}
e2e/extended: scan/remote e2e

# Runs E2E tests against a remote environment
e2e/remote: DROP_DB=false
e2e/remote: e2e/extended

# Runs E2E tests against DEV server environment
e2e/dev: ML_URL=$(DEV_ML_URL)
e2e/dev: e2e/remote

# Runs E2E tests against QA server environment
e2e/qa: ML_URL=$(QA_ML_URL)
e2e/qa: e2e/remote

#####################################
##### DEV TOOLS
#####################################

# Detects faces on given images, with selected scanners, and output the results using local ML servicef'{img_name}'
demo: IMG_NAMES=015_6.jpg
demo: scan
scan:
	python -m ml.tools.facescan.scan.run

# Detects faces on given images, with selected scanners, and output the results using remote ML service endpoint
scan/remote: USE_REMOTE=true
scan/remote: scan

# Optimizes face detection parameters with a given annotated image dataset
optimize:
	python -m ml.tools.facescan.optimize.run

# Runs experiments whether the system will crash with given images, selected face detection scanners, RAM limits, image processing settings, etc.; Intended to test for Out-Of-Memory (OOM) errors
crash-lab:
	ml/tools/crash-lab.sh $(CURDIR)/ml/sample_images

# Tests the accuracy of face detection
benchmark/detection: ml/tools/facescan/benchmark_detection/tmp
	python -m ml.tools.facescan.benchmark_detection.run

# Tests the face recognition system with accuracy error statistics at every step of processing
benchmark/e2e: ml/tools/facescan/benchmark_e2e/tmp
	python -m ml.tools.facescan.benchmark_e2e.run

#####################################
##### MISC
#####################################

# Gives a random project name
COMPOSE_PROJECT_NAME:
	@echo frs-core-$(ML_PORT)-$$(</dev/urandom tr -dc 'a-z0-9' | fold -w 1 | head -n 1)

# Finds an open port
PORT:
	@echo $$(while true; do port=$$(( RANDOM % 30000 + 30000 )); echo -ne "\035" | telnet 127.0.0.1 \
		$$port > /dev/null 2>&1; [ $$? -eq 1 ] && echo "$$port" && exit 0; done )

# Give a unique api_key
API_KEY: MONGODB_DBNAME

# Print DEV deployment environment URL
DEV_ML_URL:
	@echo $(DEV_ML_URL)

# Print QA deployment environment URL
QA_ML_URL:
	@echo $(QA_ML_URL)

# Give a unique mongodb dbname
MONGODB_DBNAME:
	@echo tmp-$(COMPOSE_PROJECT_NAME)-$$(date +'%Y-%m-%d-%H-%M-%S-%3N')

# Starts a database container
db:
	@echo -ne "\035" | telnet 127.0.0.1 $(MONGODB_PORT) > /dev/null 2>&1; [ $$? -eq 1 ] && \
	docker-compose up -d mongodb && \
	echo "[Database up] SUCCESS! Started db on port $(MONGODB_PORT)" || \
	echo "[Database up] skipped, because port $(MONGODB_PORT) is taken"

# Shows code line stats
stats:
	@(which tokei >/dev/null || conda install -y -c conda-forge tokei) && \
	tokei --exclude srcext/

# Status of DEV deployment environment
status/dev:
	@curl $(DEV_ML_URL)/status

# Status of QA deployment environment
status/qa:
	@curl $(QA_ML_URL)/status

#####################################
##### FILE DEPENDENCIES
#####################################

define get_from_remote_tgz
	mkdir -p $(2)
	curl -o $(2)/tmp.tgz $(1)
	tar zxvf $(2)/tmp.tgz -C $(2)
	rm $(2)/tmp.tgz
endef

define get_from_remote_zip
	mkdir -p $(2)
	curl -o $(2)/tmp.zip $(1)
	unzip $(2)/tmp.zip -d $(2)
	rm $(2)/tmp.zip
endef

ml/tools/facescan/tmp:
	$(call get_from_remote_zip,https://www.fontsquirrel.com/fonts/download/arimo,ml/tools/facescan/tmp/arimo-font)

ml/tools/facescan/benchmark_e2e/tmp:
	$(call get_from_remote_tgz,http://vis-www.cs.umass.edu/lfw/lfw.tgz,ml/tools/facescan/benchmark_e2e/tmp)
	curl -o ml/tools/facescan/benchmark_e2e/tmp/people.txt http://vis-www.cs.umass.edu/lfw/people.txt

ml/tools/facescan/benchmark_detection/tmp:
	$(call get_from_remote_tgz,http://tamaraberg.com/faceDataset/originalPics.tar.gz,ml/tools/facescan/benchmark_detection/tmp/originalPics)
	$(call get_from_remote_tgz,http://vis-www.cs.umass.edu/fddb/FDDB-folds.tgz,ml/tools/facescan/benchmark_detection/tmp)
