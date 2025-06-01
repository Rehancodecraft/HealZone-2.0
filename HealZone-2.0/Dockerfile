# Stage 1: Build the application
FROM maven:3.8.6-openjdk-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package shade:shade -DskipTests

# Stage 2: Run the application
FROM openjdk:21-jdk-slim
WORKDIR /app
# Install JavaFX dependencies and X11 for GUI
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libx11-xcb1 \
    && rm -rf /var/lib/apt/lists/*
# Copy the fat JAR from the build stage
COPY --from=builder /app/target/HealZone-1.0-SNAPSHOT.jar app.jar
# Create directory for SQLite database
RUN mkdir -p /app/db
ENV SQLITE_DB_PATH=/app/db/healzone.db
# Expose port (adjust if your app uses a server)
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]