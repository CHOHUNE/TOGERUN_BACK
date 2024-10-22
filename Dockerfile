FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

# Copy the JAR file
COPY *.jar app.jar

# Extract the application.yml from the JAR
RUN mkdir -p BOOT-INF/classes/ && \
    java -jar app.jar --extract BOOT-INF/classes/application.yml && \
    mv BOOT-INF/classes/application.yml /app/ && \
    rm -rf BOOT-INF

EXPOSE 8080

# Run with explicit config location
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.yml"]