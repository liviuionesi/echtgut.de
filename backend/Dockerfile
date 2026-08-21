# Multi-stage Dockerfile for echtgut-backend
FROM docker.io/library/eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S echtgut && adduser -S echtgut -G echtgut
USER echtgut:echtgut

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
