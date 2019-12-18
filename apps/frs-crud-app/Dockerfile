FROM openjdk:11
VOLUME /tmp
COPY . .
RUN apt update -y && apt install maven rsync -y && mvn clean package
RUN mkdir -p /app/lib && rsync -vaz apps/frs-crud-app/target/dependency/BOOT-INF/lib/ /app/lib/ && rsync -vaz apps/frs-crud-app/target/dependency/META-INF /app && rsync -vaz apps/frs-crud-app/target/dependency/BOOT-INF/classes/ /app/
EXPOSE 8080
ENTRYPOINT ["java","-cp","app:app/lib/*","com.exadel.frs.FrsApplication"]
