FROM maven:3.8.5-openjdk-17-slim as build
ARG ND4J_CLASSIFIER
WORKDIR /workspace/compreface
LABEL intermidiate_frs=true
RUN apt update && apt upgrade -y && apt-get install binutils -y
RUN $JAVA_HOME/bin/jlink --verbose --add-modules ALL-MODULE-PATH --strip-debug --no-man-pages --no-header-files --compress=2 --output /jre
COPY pom.xml .
COPY api/pom.xml api/
COPY admin/pom.xml admin/
COPY common/pom.xml common/
RUN mvn -B clean install -DskipTests -Dcheckstyle.skip -Dasciidoctor.skip -Djacoco.skip -Dmaven.gitcommitid.skip -Dspring-boot.repackage.skip -Dmaven.exec.skip=true -Dmaven.install.skip -Dmaven.resources.skip
COPY api api
COPY admin admin
COPY common common
RUN mvn package -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true -Dnd4j.classifier=$ND4J_CLASSIFIER

FROM debian:bullseye-slim as frs_core
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=build /jre $JAVA_HOME
ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/api/target/*.jar /home/app.jar
ENTRYPOINT ["sh","-c","/jre/bin/java $API_JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /home/app.jar"]

FROM debian:bullseye-slim as frs_crud
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=build /jre $JAVA_HOME
ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/admin/target/*.jar /home/app.jar
ARG APPERY_API_KEY
ENV APPERY_API_KEY ${APPERY_API_KEY}
ENTRYPOINT ["sh","-c","/jre/bin/java $ADMIN_JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar /home/app.jar"]
