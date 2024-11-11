FROM amazoncorretto:17

# Copy the jar file into the container
COPY target/Monolith-0.0.1-SNAPSHOT.jar app.jar

# Set the entry point to run the app
ENTRYPOINT ["java", "-jar", "/app.jar"]
