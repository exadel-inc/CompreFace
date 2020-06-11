FROM maven:3.6.1-jdk-13-alpine as build
WORKDIR /workspace/frs
LABEL intermidiate_frs=true
COPY pom.xml .
COPY api/pom.xml api/
COPY admin/pom.xml admin/
RUN ["/usr/local/bin/mvn-entrypoint.sh", "mvn", "verify", "clean", "--fail-never"]
COPY api api
COPY admin admin
RUN mvn package -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true

FROM bellsoft/liberica-openjdk-alpine:11 as frs_core
ARG DIR=/workspace/frs
COPY --from=build ${DIR}/api/target/*.jar /home/app.jar
ENTRYPOINT ["java","-jar","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","-Dspring.profiles.active=local","/home/app.jar"]

FROM bellsoft/liberica-openjdk-alpine:11 as frs_crud
ARG DIR=/workspace/frs
COPY --from=build ${DIR}/admin/target/*.jar /home/app.jar
ENTRYPOINT ["java","-jar","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "/home/app.jar"]