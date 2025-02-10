# Используем базовый образ с Java
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY target/QuizBot-1.0-SNAPSHOT.jar /app/QuizBot-1.0-SNAPSHOT.jar

# Команда для запуска приложения
CMD ["java", "-jar", "QuizBot-1.0-SNAPSHOT.jar"]