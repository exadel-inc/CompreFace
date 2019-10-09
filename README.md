## Docker

To run project via docker(installed Docker required) you need to:
 - build java application using ```mvn clean package``` (if Maven installed) or build with maven wrapper ```./mvnw clean package```.
 - run ```docker-compose up``` in your console.
 - that's it. Application will start soon in port 8080 in your device or VM.
 
## API

To open swagger api view follow http://localhost:8080/swagger-ui.html