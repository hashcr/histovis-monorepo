# syntax=docker/dockerfile:1
# apps/images-service/Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copy the whole repo so Maven can resolve modules referenced by images-service.
# The --mount=type=cache keeps the Maven local repo (~/.m2) persistent across
# builds, so dependencies are NOT re-downloaded when only source files change.
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl apps/images-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/apps/images-service/target/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
