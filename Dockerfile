# Use official Java 21 runtime
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy Maven build files
COPY pom.xml .
COPY src ./src

# Install Maven and build the JAR
RUN apt-get update && apt-get install -y maven && mvn -f pom.xml clean package -DskipTests

# Run the Spring Boot JAR
ENTRYPOINT ["java", "-jar", "/app/target/kafka-order-system-1.0.0.jar"]
