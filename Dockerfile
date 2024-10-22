FROM bellsoft/liberica-openjdk-alpine:17

VOLUME /tmp
COPY *.jar app.jar
COPY application.yml application.yml
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]