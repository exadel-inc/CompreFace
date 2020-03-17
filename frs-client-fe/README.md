# FrsClientFe

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 8.3.19.

## Development Requirements

- Node.js
- Docker
- Bash compatible command prompt

## Development Run

Run starting script: `./scripts/start.sh`

## Mock API server 

Run `mock-server` for API server. Server host:  `http://localhost:3000/`. The app will automatically reload if you change server.js file. \
JSON files are used for creating data storage in `mock-backend/data` folder. Server uses ExpressJs framework and nodemon. \
API is described in confluence: https://confluence.exadel.com/pages/viewpage.action?spaceKey=KC&title=FRS+REST+API

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

#Project structure and architecture

###Folder structure
`styles:` folder with sass common styles \
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
 
