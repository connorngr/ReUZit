# Step 1: Use an official Maven image to build the app
FROM maven:3.8.1-openjdk-11 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package

# Step 2: Use the JAR from the build stage
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/reuzit.jar /app/reuzit.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "reuzit.jar"]