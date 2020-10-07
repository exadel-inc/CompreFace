## Overview

Face Recogntion system is a way of recognizing a human face through technology. A facial recognition system uses facial features from a photograph and compares the information with a database of known faces to find a match.

So how does facial recognition work?

Step 1. You need to sign up to the system (First user in CompareFace admin has Owner role, but it is possible to change the role) and then LogIn with created account or just use the existing one. After that system redirects you to the main page.

Step 2. Create an application (left section) with "Create" link at the bottom of the page. An application is where you can create and manage your face collections.

Step 3. Enter you application with double click on the name of the application. Here you will have two possibilities. The first one is to add new users to your application and manage permissions (Owner and Administrator roles already have access to any application without invite, user role doesn't.) The second one is to create face collections.

Step 4. After creating new collection, it appears at the Face Collections List created within the application with an appropriate name and API key. The user has the possibility to add new Face or to test the existing one (three dots on right side and click "test" link). This option will lead the user to Test Model page, where is the drag&drop to upload image with face to recognize.

# Project structure and architecture

### Folder structure
`styles:` folder with scss common styles \
`core` folder with Global framework-based services \
`data` global models, enums, classes, interfaces (user model, permissions, roles ect. NO Dto's data inside!!!!). Don't based on specific framework/plugin \
`feature/containers` Feature specific smart/container components. Communicates with store through facade. Styles less.\
`feature/compoentns` Feature specific dump/presentational components. Communicates with containers  through Input/Output. Logic less.\
`pages/` Page specific router modules (home, 404, login ect.) with component which are responsible for page layout and composition of features

`store` Store folder. index.ts contains union store and reducers\
`store/featureName` Store for one feature.
`store/featureName/module` Feature store encapsulated into feature module and declared `StoreModule.forFeature('Feature', FeatureReducer)`
`store/featureName/actions` Store actions. Can be handled by effect and/or reducer. Can be called in facade.\
`store/featureName/effects` Actions handler which needs to produce some side effect(API call, etc.) and then call facade method if needed.\
`store/featureName/selectors` Selectors for feature state
`store/featureName/reducers` Actions handler which directly changes store data. Pure function.\
`store/featureName/feature-entitys.service.ts` service for access for entities https://ngrx.io/guide/data/entity-services


## Project setup and usage

## Getting started

To get started, perform the following steps:

1. Install Docker
1. Download archive from our latest release: https://github.com/exadel-inc/CompreFace/releases
1. Unzip archive
1. Run command:
`
docker-compose up --build
`
1. Open http://localhost:8000/

#### To start UI development mode (with live watchers):  
(only for first time run)
- Run `cd ui`
- Run `npm install`

- Run `cd ../dev`
- Run `start--dev.sh` to start docker container with backend and UI application in live reload mode
- After that open browser http://localhost:4200/


#### Usage.

- On http://localhost:4200/ you need to signup.
- After you sign up you have to login, and go to main page.
