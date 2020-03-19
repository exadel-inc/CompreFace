.DEFAULT_GOAL := local
.PHONY: local-setup local-run local-test local-test-unit local-test-integration local-test-e2e lint

# Run tests and checks on the local machine
local: local-setup local-test-unit local-test-integration local-test-e2e lint

# Setup requirements needed to run the service locally
local-setup:
	python -m pip install -r ./docker/ml/requirements.txt
	python -m pip install -r ./docker/e2e/requirements.txt
	python -m pip install -e ./src/services/facescan/backend/insightface/extlib/insightface/python-package

# Start service locally in debug mode
local-run: ml.pid
ml.pid:
	$(CURDIR)/test/e2e/local-run.sh $(CURDIR)/ml.pid

# Run unit tests
local-test-unit:
	python -m pytest -m "not integration"

# Run integration tests
local-test-integration:
	python -m pytest -m "integration"

# Run e2e tests
local-test-e2e: local-run
	sleep 5
	kill -9 $$(cat $(CURDIR)/ml.pid)

# Run lint checks
lint:
	python -m pylama
