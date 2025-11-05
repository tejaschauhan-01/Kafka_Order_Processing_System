# ============================
# Stage 1: Build the application
# ============================
FROM maven:3.9.6-eclipse-temurin-21 AS build
# Set working directory
WORKDIR /app
# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src
# Build the application JAR (skip tests to save time)
RUN mvn clean package -DskipTests
# ============================
# Stage 2: Run the application
# ============================
FROM eclipse-temurin:21-jdk
# Set working directory for the runtime container
WORKDIR /app
# Copy only the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar
# Expose the Spring Boot app port
EXPOSE 8080
# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
