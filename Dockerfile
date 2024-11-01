FROM bellsoft/liberica-openjdk-alpine:17

# curl 설치
RUN apt-get update && apt-get install -y curl

VOLUME /tmp
COPY app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]