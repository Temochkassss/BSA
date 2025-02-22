# Сборка JAR
FROM maven:3.9.5-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Копируем собранный JAR-файл
COPY --from=builder /app/target/QuizBot-1.0-SNAPSHOT.jar ./bot.jar

# Создаем папку data и устанавливаем права
RUN mkdir -p /app/data && chmod a+rw /app/data

# Копируем базу данных из локальной папки data в контейнер
COPY data/question.db /app/data/question.db

# Устанавливаем права на базу данных (опционально, если нужно)
RUN chmod a+rw /app/data/question.db

# Запуск приложения
CMD ["java", "-jar", "bot.jar"]