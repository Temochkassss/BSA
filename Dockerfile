# Сборка JAR
FROM maven:3.8.6-openjdk-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM openjdk:21-jre-slim

WORKDIR /app
COPY --from=builder /app/target/QuizBot-1.0-SNAPSHOT.jar ./bot.jar
CMD ["java", "-jar", "bot.jar"]