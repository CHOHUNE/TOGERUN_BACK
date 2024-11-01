FROM bellsoft/liberica-openjdk-alpine:17

# curl 설치
RUN apk add --no-cache curl

VOLUME /tmp
COPY app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]