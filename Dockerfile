FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

# Copy jar file
COPY *.jar app.jar

# Create directory for configuration
RUN mkdir -p src/main/resources

# Copy application.yml
COPY src/main/resources/application.yml src/main/resources/application.yml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/src/main/resources/application.yml"]

