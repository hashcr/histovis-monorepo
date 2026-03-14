# apps/images-service/Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copy the whole repo so Maven can resolve modules referenced by images-service
COPY . .
# Build only the images-service and its dependencies, skip tests for speed
RUN mvn -pl apps/images-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/apps/images-service/target/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
