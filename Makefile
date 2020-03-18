.DEFAULT_GOAL := test_local
.PHONY: local-setup local-run local-test local-test-unit local-test-integration lint

# Setup requirements needed to run the service locally
local-setup:
	python -m pip install -r requirements.txt
	python -m pip install -r requirements-e2e.txt
	python -m pip install -e ./src/services/facescan/backend/insightface/extlib/insightface/

# Start service locally in debug mode
local-run:
	python -m src.main

# Run tests and checks on the local machine
local-test: local-test-unit local-test-integration lint
	test_unit test_integration test_lint

# Run unit tests
local-test-unit:
	python -m pytest -m "not integration"

# Run integration tests
local-test-integration:
	python -m pytest -m "integration"

# Run lint checks
lint:
	python -m pylama
