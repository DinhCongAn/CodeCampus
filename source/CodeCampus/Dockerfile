# 1. Dùng Maven với JDK 21 để build
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# 2. Dùng OpenJDK 21 để chạy
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/CodeCampus-0.0.1-SNAPSHOT.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]