FROM maven:3.8.2-eclipse-temurin-17 as build
ARG ND4J_CLASSIFIER
WORKDIR /workspace/compreface
LABEL intermidiate_frs=true
COPY pom.xml .
COPY api/pom.xml api/
COPY admin/pom.xml admin/
COPY common/pom.xml common/
RUN mvn -B clean install -DskipTests -Dcheckstyle.skip -Dasciidoctor.skip -Djacoco.skip -Dmaven.gitcommitid.skip -Dspring-boot.repackage.skip -Dmaven.exec.skip=true -Dmaven.install.skip -Dmaven.resources.skip
COPY api api
COPY admin admin
COPY common common
RUN mvn package -Dmaven.test.skip=true -Dmaven.site.skip=true -Dmaven.javadoc.skip=true -Dnd4j.classifier=$ND4J_CLASSIFIER

FROM eclipse-temurin:17-jre-focal as frs_core
ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/api/target/*.jar /home/app.jar
ENTRYPOINT ["sh","-c","java $API_JAVA_OPTS -jar /home/app.jar"]

FROM eclipse-temurin:17-jre-focal as frs_crud
ARG DIR=/workspace/compreface
COPY --from=build ${DIR}/admin/target/*.jar /home/app.jar
ARG APPERY_API_KEY
ENV APPERY_API_KEY ${APPERY_API_KEY}
ENTRYPOINT ["sh","-c","java $ADMIN_JAVA_OPTS -jar /home/app.jar"]
