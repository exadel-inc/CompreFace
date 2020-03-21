.DEFAULT_GOAL := default
.PHONY: run test local local-setup local-run local-test-unit local-test-integration local-test-e2e lint

default: lint test

# Build and run newest version
run:
	docker-compose up ml

# Build containers and run tests against them
test:
	docker-compose build

# Run tests and checks on the local machine
local: local-setup local-test-unit local-test-integration local-test-e2e lint

# Setup requirements needed to run the service locally
local-setup:
	python -m pip install -r ./e2e/requirements.txt
	python -m pip install -r ./ml/requirements.txt
	python -m pip install -e ./ml/src/services/facescan/backend/insightface/extlib/insightface/python-package

# Start service locally in debug mode
local-run: local-run.ml.pid
local-run.ml.pid:
	$(CURDIR)/ml/local-run.sh

# Run unit tests
local-test-unit:
	python -m pytest -m "not integration" $(CURDIR)/ml/src

# Run integration tests
local-test-integration:
	python -m pytest -m "integration" $(CURDIR)/ml/src

# Run e2e tests
local-test-e2e: local-run
	$(CURDIR)/e2e/run-e2e-test.sh http://localhost:3000
	kill -9 $$(cat $(CURDIR)/ml.pid)

# Run lint checks
lint:
	python -m pylama $(CURDIR)/ml
