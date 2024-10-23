FROM bellsoft/liberica-openjdk-alpine:17

VOLUME /tmp
COPY app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
