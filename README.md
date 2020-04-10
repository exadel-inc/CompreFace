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
1. Shut down the service with `$ make down` <br>

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
Entrypoints to run the application and related development tools are organized as "targets" inside `Makefile` and run with `$ make`.

### Using `make`
Run multiple targets one after another:

`$ make build up`

Set environment variables for one run:

`$ make start ML_PORT=3001`

Set environment variables for shell session:

`$ export ML_PORT=3001`

Set environment variables to random values for shell session. You'll be able to run multiple instances of the same service (and start multiple tests simultaneously) in different terminals, after running this command in each one first:

`$ . new-make-environment.sh`
 
### Environment variables
Select port for starting the service:

`ML_PORT=3000`

Select database connection. `MONGODB_URI` has higher precedence.

`MONGODB_HOST=localhost`<br>
`MONGODB_PORT=27017`<br>
`MONGODB_DBNAME=efrs_db`<br>
`MONGODB_URI=mongodb://localhost:27017/efrs_db`<br>

Select face scanner backend:

`SCANNER=InsightFace`

Configure image rescaling before face detection:

`IMG_LENGTH_LIMIT=720`

Limit amount of RAM memory available. Disables swap.

`MEM_LIMIT=1024m`

Location of external service:

`ML_URL=http://localhost:3000`

Run tests during build of `ml` container:

`DO_RUN_TESTS=true`

### Targets
Runs main tests to check whether the project is in a valid state. Target `default` runs additional short tests first, locally, to fail faster.

`default`, `test`

Build, start, and stop docker containers:

`build`, `up`, `down`

Setup, start, and stop app in local environment:

`setup`, `start`, `stop`

Run tests locally. `test/local` runs the other tests.

`test/local`, `test/unit`, `test/lint`, `test/i9n`, `test/e2e`

Run E2E tests locally or against a remote target. `e2e/local` is alias for `test/e2e`.

`e2e`, `e2e/local`<br> 
(e.g. `$ make e2e ML_URL=http://example.com/api API_KEY=f74a-af5f DROP_DB=false`)

Detect faces on given images, with selected scanners, and output the results:

`scan`

Optimize face detection parameters with a given annotated image dataset:

`optimize`

Run experiments whether the system will crash with given images, selected face detection scanners, RAM limits, image processing settings, etc.:

`crash_lab`

Find open ports, give randomized project names, API keys:

`PORT`, `COMPOSE_PROJECT_NAME`, `API_KEY`

Start database container:

`db`

Show code stats:

`stats`

# Development on Windows
- Containers may not build/run because of CRLF file endings. To fix, run `$ dos2unix * ml/* e2e/*`.
- uWSGI does not support Windows, workaround is removing it from `ml/requirements.txt` before running `$ make setup` and adding it back afterwards.
