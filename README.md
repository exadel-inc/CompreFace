##### How to start the service

1. Installs dependencies: `$ make local-setup`
2. Starts app in debug mode: `$ make local-run`

---

##### How to run the tests
- Builds containers and run tests in them: `$ make`
- Runs tests locally (without containers): `$ make local`

---

##### Where to find more commands
Run `$ make [TARGET]` with one of the targets available in `./Makefile`.

---

##### Notes for Windows users
- Containers may not build/run because of CRLF file endings. To fix, run `$ dos2unix * ml/* e2e/*` and/or `> git config core.autocrlf false`.
- uWSGI does not support Windows, workaround is having it removed from `requirements.txt` temporarily while running `make` with local targets.
