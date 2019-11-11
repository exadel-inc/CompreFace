FROM openjdk:11
VOLUME /tmp
COPY . .
RUN apt update -y && apt install maven rsync -y && mvn clean package
RUN mkdir -p /app/lib && rsync -vaz target/dependency/BOOT-INF/lib/ /app/lib/ && rsync -vaz target/dependency/META-INF /app && rsync -vaz target/dependency/BOOT-INF/classes/ /app/
ENTRYPOINT ["java","-cp","app:app/lib/*","com.exadel.frs.FrsApplication"]
