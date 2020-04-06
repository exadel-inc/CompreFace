# External documentation
About EFRS:<br>
- [EFRS Confluence Space](https://confluence.exadel.com/display/EFRS/Python+Core+Service)<br>
- [EFRS Page in KC Confluence Space](https://confluence.exadel.com/display/EFRS/Python+Core+Service)<br>
- [EFRS API Contract - Confluence](https://confluence.exadel.com/display/KC/FRS+REST+API)<br>

About frs-core:<br>
- [Confluence page on Python Core Service](https://confluence.exadel.com/display/EFRS/Python+Core+Service)<br>
- frs-core API Contract:
  - [Swagger UI - localhost](http://localhost:3000/apidocs) (service must be started locally first)<br>
  - [Swagger UI - UAT]( https://uat.frs.exadel.by/apidocs) 

# Starting the service
### With Docker
1. Build the container: `$ make build`
2. Start it: `$ make up`
3. Stop it: `$ make down`

You can combine steps 1 and 2: `$ make build up`

### In development environment, container-less
1. Setup dependencies: `$ make setup`
2. Start main app in debug mode: `$ make start`
3. Stop it: `$ make stop`

# Testing the service
### With Docker
Builds containers, run tests inside: `$ make docker` 

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
Make supports these arguments (let's use `$ make up` as an example):
- `$ make up ID=2` - Helps solve container name collisions (appends the ID at the end, so that container `ml` becomes `ml2`)
- `$ make up PORT=8080 MONGO_PORT=6650` - Sets the exposed port for the service and the database

### Additional make targets and arguments
Check `Makefile` for more make targets and arguments that are not mentioned in this README.

### Notes for Windows users
- Containers may not build/run because of CRLF file endings. To fix, run `$ dos2unix * ml/* e2e/*`.
- uWSGI does not support Windows, workaround is removing it from `ml/requirements.txt` before running `$ make setup` and adding it back afterwards.
