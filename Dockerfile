FROM eclipse-temurin:17-jdk-alpine
RUN apk add --update ffmpeg
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENV MYSQL_HOST host.docker.internal
ENTRYPOINT ["java","-jar","/app.jar"]