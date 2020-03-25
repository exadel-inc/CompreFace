# Documentation
- Face Recognition System 
- frs-core


# Starting the service

### With Docker

1. Build the container: `$ make build`
2. Start it: `$ make up`
3. Stop it: `$ make down`

You can combine steps 1 and 2: `$ make build up`

### In development environment, container-less

1. Setup dependencies: `$ make setup`
2. Start main app in debug mode: `$ make run`
3. Stop it: `$ make stop`

# Testing the service

### With Docker

Builds containers, run tests inside: `$ make` 

### In development environment, container-less

1. Setup dependencies: `$ make setup`
2. Run all tests: `$ make local`
   - Unit tests: `$ make unit`
   - Integration tests:` $ make i9n`
   - End-to-end tests: `$ make e2e`
   - Lint check: `$ make lint`

# Miscellaneous

### Notes for Windows users
- Containers may not build/run because of CRLF file endings. To fix, run `$ dos2unix * ml/* e2e/*`.
- uWSGI does not support Windows, workaround is removing it from `ml/requirements.txt` before running `$ make setup` and adding it back afterwards.
