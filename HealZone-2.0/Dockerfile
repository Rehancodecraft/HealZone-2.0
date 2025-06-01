# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package shade:shade -DskipTests

# Stage 2: Run the application
FROM openjdk:21-jdk-slim
WORKDIR /app
# Install GUI dependencies for JavaFX
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libgtk-3-0 \
    libx11-xcb1 \
    libxtst6 \
    libxslt1.1 \
    && rm -rf /var/lib/apt/lists/*
# Download and install JavaFX SDK
ENV JAVA_FX_VERSION=21
RUN apt-get update && apt-get install -y wget unzip \
    && wget -q https://download2.gluonhq.com/openjfx/${JAVA_FX_VERSION}/openjfx-${JAVA_FX_VERSION}_linux-x64_bin-sdk.zip \
    && unzip openjfx-${JAVA_FX_VERSION}_linux-x64_bin-sdk.zip -d /opt \
    && rm openjfx-${JAVA_FX_VERSION}_linux-x64_bin-sdk.zip \
    && apt-get remove -y wget unzip \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*
ENV JAVA_FX_HOME=/opt/javafx-sdk-${JAVA_FX_VERSION}
# Copy the fat JAR from the build stage
COPY --from=builder /app/target/HealZone-1.0-SNAPSHOT.jar app.jar
# Create directory for SQLite database
RUN mkdir -p /app/db
ENV SQLITE_DB_PATH=/app/db/healzone.db
# Expose port (adjust if needed)
EXPOSE 8080
# Run the application with JavaFX modules
ENTRYPOINT ["java", "--module-path", "/opt/javafx-sdk-21/lib", "--add-modules", "javafx.controls,javafx.fxml", "-jar", "app.jar"]