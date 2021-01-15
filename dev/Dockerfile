FROM maven:3.6.3-jdk-11-slim as build
WORKDIR /workspace/compreface
LABEL intermidiate_frs=true
COPY pom.xml .
COPY api api
COPY admin admin
RUN mvn -B clean package -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true

FROM openjdk:11.0.8-jre-slim as frs_core
ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/api/target/*.jar /home/app.jar
ENTRYPOINT ["sh","-c","java $API_JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /home/app.jar"]

FROM openjdk:11.0.8-jre-slim as frs_crud
ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/admin/target/*.jar /home/app.jar
ENTRYPOINT ["sh","-c","java $ADMIN_JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /home/app.jar"]
