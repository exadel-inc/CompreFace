# CompreFace

#### Getting started for Contributors:

1. Install Docker and Docker-Compose
2. Clone repository
3. Open dev folder
4. Run command: `sh start.sh`

#### Tips for Windows (use Git Bash terminal)

1. Turn of the git autocrlf with command: `git config --global core.autocrlf false`
2. Make sure all your containers are down: `docker ps`
3. In case some containers are working, they should be stopped: `docker-compose down`
4. Clean all local datebases and images: `docker system prune --volumes`
5. Go to Dev folder `cd dev`
6. Run `sh start.sh` and make sure http://localhost:4200/ starts (Only for UI contributors)

#### How to start for UI development:

Open a new terminal window and run next commands:

- Run `cd ui`
- Run `npm install` (only for first time run)
- Run `npm start`

Go to http://localhost:4200/ and you can sign up with any credentials
