# Сборка JAR
FROM maven:3.9.5-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /app/target/QuizBot-1.0-SNAPSHOT.jar ./bot.jar

# Копируем базу данных из папки data в контейнер
COPY data/question.db /app/data/question.db

# Создаем папку data и копируем базу данных
RUN mkdir -p /app/data && chmod a+rw /app/data

CMD ["java", "-jar", "bot.jar"]