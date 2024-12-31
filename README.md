# Local Gallery Server (Spring version)

Backend server written in Java/Kotlin with Spring Boot for [Local Gallery Android app](https://github.com/memostark/LocalGallery) that provides child folders and images of the selected directory.

# Run using source code
Run using Gradle, first build the program:

```
gradlew clean build
```
Set up the environment variable `BASE_PATH` to the location of your root folder and run:

```
gradlew bootRun
```

The program can also be run using Docker:

```
docker build -t gallery_app .
docker run -p 80:80 -v /path/to/folder:/tests -e "BASE_PATH=/tests" -e OUTBOUND_IP_ADDRESS=your_ip gallery_app
```
