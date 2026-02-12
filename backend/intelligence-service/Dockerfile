FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy build files
COPY build.gradle .

# Copy source code
COPY src src

# Build the application
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
