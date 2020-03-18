.DEFAULT_GOAL := test-local
.PHONY: setup-local run-local test-local test-unit test-integration test-e2e test-lint

# Setup requirements needed to run the service locally
setup-local:
	python -m pip install -r requirements.txt
	python -m pip install -r requirements-build.txt
	python -m pip install -e ./src/shared/extlib/insightface/python-package

# Start service locally
run-local:
	python -m src.shared.api.main

# Run tests and checks on the local machine
test-local: test-unit test-integration test-lint

# Run unit tests
test-unit:
	python -m pytest -m "not integration"

# Run integration tests
test-integration:
	python -m pytest -m integration

# Run e2e test against already running host
test-e2e:
	python -m pytest ./test/e2e/

# Run lint checks
test-lint:
	python -m pylama

# Build docker container
test-local-docker:
	./build-and-test.sh -d
