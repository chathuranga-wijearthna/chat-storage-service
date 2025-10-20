# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -B -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]