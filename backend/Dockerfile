# Multi-stage Dockerfile for echtgut-backend
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S echtgut && adduser -S echtgut -G echtgut
USER echtgut:echtgut

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
