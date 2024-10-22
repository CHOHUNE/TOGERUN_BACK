FROM bellsoft/liberica-openjdk-alpine:17

VOLUME /tmp
COPY build/libs/*.jar app.jar
COPY src/main/resources/application.yml application.yml
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]