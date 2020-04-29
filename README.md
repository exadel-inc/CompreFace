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
1. Start the service in debug mode: `$ make start`
1. Service is now available at `http//localhost:3000`
1. Stop it with `$ make stop`

Note: [mongoDB](https://www.mongodb.com/download-center/community) Database will automatically be instantiated with [Docker](https://docs.docker.com/install/linux/docker-ce/ubuntu/) before `$ make start` if it is not already running at port `$MONGODB_PORT` (default: `27017`).

#### Run main tests

##### Locally
To check whether the project is in a valid state, run `$ make`.

##### Remote environments
To check whether the project passes E2E tests on a remote deployment environment, run:

- `$ make e2e/dev` (DEV environment)
- `$ make e2e/qa` (QA environment)
- `$ make e2e/remote ML_URL=http://example.com:3000` (other server)

#### Scan a demo image

To get the image shown at the top of the README.md, run: `$ make demo`.

# Advanced usage
Entrypoints to run the application and related development tools are organized as "targets" inside `Makefile` and run with `$ make`.

### Using `make`
Run multiple targets one after another:<br>
`$ make db test/e2e`

Set environment variables (for current run):<br>
`$ make e2e ML_URL=http://example.com/api API_KEY=f74a-af5f DROP_DB=false`

Set environment variables (for shell session, future runs):<br>
`$ export ML_PORT=3001`

Self-generate values for arguments. For example, this command will scan image using the service deployed in DEV environment, using a random API key:<br>
`$ make scan IMG_NAMES=image.png ML_URL=$(make DEV_ML_URL) API_KEY=$(make API_KEY) USE_REMOTE=true`

Set environment variables to random values for shell session. You'll be able to run multiple instances of the same service (and start multiple tests simultaneously) in different terminals, after running this command in each one first:<br>
`$ . new-make-environment.sh`
 
### Most relevant environment variables
Select port for starting the service:<br>
`ML_PORT=3000`

Select database connection. `MONGODB_URI` has higher precedence.<br>
`MONGODB_HOST=localhost`,
`MONGODB_PORT=27017`,
`MONGODB_DBNAME=efrs_db`,
`MONGODB_URI=mongodb://localhost:27017/efrs_db`

Drop database before E2E tests:<br>
`DROP_DB=true`

### More targets and env variables

More information is available inside `Makefile`.

# Development on Windows
- Containers may not build/run because of CRLF file endings. To fix, run `$ dos2unix * ml/* e2e/* ml/tools/*`.
- uWSGI does not support Windows, workaround is removing it from `ml/requirements.txt` before running `$ make setup` and adding it back afterwards.
