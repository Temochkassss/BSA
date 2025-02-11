# Сборка JAR
FROM maven:3.8.6-openjdk-11 AS builder

WORKDIR /app
COPY pom.xml .
# Копируем исходники
COPY src ./src
# Собираем проект (JAR появится в /app/target/)
RUN mvn clean package -DskipTests

# Финальный образ
FROM openjdk:11-jre-slim

WORKDIR /app
# Копируем JAR из стадии сборки
COPY --from=builder /app/target/QuizBot-1.0-SNAPSHOT.jar ./bot.jar
# Если SQLite хранит данные в файле, создаем директорию и разрешаем запись
RUN mkdir -p /app/data && chmod a+rw /app/data

# Запуск приложения
CMD ["java", "-jar", "bot.jar"]