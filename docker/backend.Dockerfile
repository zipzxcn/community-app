FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_FILE=community-backend/target/community-backend-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app/app.jar

ENV TZ=Asia/Shanghai

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
