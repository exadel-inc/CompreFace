![Example output image](./ml/sample_images/readme_example.png)
# frs-core
This is a component of Exadel Face Recognition Service. EFRS is a service for face recognition: upload images with faces of known people, then upload a new image, and the service will recognize faces in it.

#### External Documentation
- Public page about EFRS - [Exadel Face Recognition Service](https://confluence.exadel.com/display/KC/Exadel+Face+Recognition+Service)
- EFRS Documentation - [EFRS Confluence Space](https://confluence.exadel.com/display/EFRS/Exadel+FRS+Home)
- EFRS API Contract - [FRS REST API](https://confluence.exadel.com/display/KC/FRS+REST+API)
- frs-core Documentation - [Python Core Service](https://confluence.exadel.com/display/EFRS/Python+Core+Service)
- frs-core API Contract
    - Swagger UI on QA Environment - [apidocs](http://qa.frs.exadel.by:3000/apidocs), [apidocs2](http://qa.frs.exadel.by:3000/apidocs2)
    - Swagger UI run locally (app must be started) - [apidocs](http://localhost:3000/apidocs), [apidocs2](http://localhost:3000/apidocs2)

# Getting Started
These instructions will get you the project up and running on your local development machine.

#### Run the service from containers
1. Up the containers with `$ make up`
1. You can make requests to the service at `http://localhost:3000` as described in [apidocs](http://localhost:3000/apidocs), [apidocs2](http://localhost:3000/apidocs2)
1. Shut down the service with `$ make down`

Note: Once you'll make changes to the project, you'll need to  run `$ make build` to have them applied on the next run of `$ make up`.

#### Run the service locally
Run `$ make setup` to install required packages to your system.

1. Have the database running at `localhost:27117`. You can do it with either: 
    - `$ make db`, if you have [Docker](https://docs.docker.com/install/linux/docker-ce/ubuntu/) (with `docker-compose`) installed
    - or `$ sudo mongod --port 27717`, if you have [mongoDB](https://www.mongodb.com/download-center/community) installed
1. Start the service in debug mode: `$ make start`
1. Service is now available at `http//localhost:3000`
1. Stop it with `$ make stop`

#### Run main tests
To check whether the project is in a valid state, run `$ make`.

# Advanced usage
##### Valid the into parts
`asdfasdf`
##### Combine the project into parts
`asdfasdf`  

### In development environment, container-less
1. Setup dependencies: `$ make setup`
2. Run all tests: `$ make local`
   - Unit tests: `$ make unit`
   - Integration tests:` $ make i9n`
   - End-to-end tests: `$ make e2e`
   - Lint check: `$ make lint`

### End-to-end tests against a remote host

Use `$ make e2e_remote`, for example:

`$ make e2e_remote ML_URL=http://example.com/api API_KEY=f74a-af5f DROP_DB=false`

# Miscellaneous
### Make arguments
Most targets support additional arguments. Let's use `$ make up` as an example:
- `$ make up ID=2` - Helps solve container name collisions (appends the ID at the end, so that container `ml` becomes `ml2`). Useful for building/running/testing different branches at the same time
- `$ make up PORT=8080 MONGODB_PORT=6650` - Sets the exposed port for the service and the database

### Additional make targets and arguments
Check `Makefile` for more make targets and arguments that are not mentioned in this README.

### Notes for Windows users
- Containers may not build/run because of CRLF file endings. To fix, run `$ dos2unix * ml/* e2e/*`.
- uWSGI does not support Windows, workaround is removing it from `ml/requirements.txt` before running `$ make setup` and adding it back afterwards.
