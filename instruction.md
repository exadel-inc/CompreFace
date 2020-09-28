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

-  Applications (left section with list of applications)
    - Let's create a new application and go to detail.
    - Now we see face collections card.
    - Let's create new face colletion.
    - And now we see list, with name, API key.
    - We can add new face or test existing.
    - For testing just click on three dots on right side and click test.
    - After it redirected to Tes Model page just upload image with face you need to recognize.

- Users (right section with list of users)
    - This is the list of users witch exist in this system.
    - Each user has permissions depending on the Role which he has.
    - First user in CompareFace admin has Owner role, but it is possible to transfer it to another user.
    
