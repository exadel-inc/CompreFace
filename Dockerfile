FROM maven:3.6.1-jdk-13-alpine as build
WORKDIR /workspace/frs
LABEL intermidiate_frs=true
COPY api frs-core-app
COPY admin frs-crud-app
COPY pom.xml .
RUN mvn clean package -DskipTests

FROM bellsoft/liberica-openjdk-alpine:11 as frs_core
ARG DIR=/workspace/frs
COPY --from=build ${DIR}/frs-core-app/target/*.jar /home/app.jar
ENTRYPOINT ["java","-jar","/home/app.jar"]

FROM bellsoft/liberica-openjdk-alpine:11 as frs_crud
ARG DIR=/workspace/frs
COPY --from=build ${DIR}/frs-crud-app/target/*.jar /home/app.jar
ENTRYPOINT ["java","-jar","/home/app.jar"]