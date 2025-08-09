FROM eclipse-temurin:24-jdk-alpine

# Install latest ffmpeg version using static builds
RUN wget https://johnvansickle.com/ffmpeg/builds/ffmpeg-git-amd64-static.tar.xz &&\
    tar xvf ffmpeg*.xz &&\
    cd ffmpeg-*-static &&\
    ls &&\
    ln -s "${PWD}/ffmpeg" /usr/local/bin/ &&\
    ln -s "${PWD}/ffprobe" /usr/local/bin/ &&\
    cd ~

VOLUME /tmp
COPY build/libs/*.jar app.jar
ENV MYSQL_HOST host.docker.internal
ENTRYPOINT ["java","-jar","/app.jar"]