FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/chem-query-platform-demo.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
