## Docker

To run project via docker(installed Docker required) you need to:
 - build java application using ```mvn clean package``` (if Maven installed) or build with maven wrapper ```./mvnw clean package```.
 - run ```docker-compose up``` in your console.
 - that's it. Application will start soon in port 8080 in your device or VM.
 
## Without Docker
Install:
 1. JDK 11+
 2. PostgreSQL 11.6+
 3. Maven
 
 
To run project via maven you need to:
 - run in command line ```mvn clean spring-boot:run``` (if Maven installed) or build with maven wrapper ```./mvnw clean spring-boot:run```.
 - if you need specify some run properties do ```mvn clean spring-boot:run -Dspring-boot.run.arguments=--spring.main.banner-mode=off,--customArgument=custom```
 
To run project via Intellij Idea
 - open class **FrsApplication** and run it


## API

To open swagger api view follow http://localhost:8080/swagger-ui.html

## Email
For set up email sending following parameters should be filled in .env file
 - 'mail_host' (smtp.gmail.com)
 - 'email_username' (example@example.com)
 - 'email_from' (according format in RFC2822, is optional)
 - 'email_password' (password for 'email_username' account)
 - 'enable_email_server' (true)