FROM bellsoft/liberica-openjdk-alpine:17

VOLUME /tmp
COPY *.jar app.jar
COPY src/main/resources/application.yml /src/main/resources/application.yml
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]