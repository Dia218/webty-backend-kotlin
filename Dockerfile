# OpenJDK 이미지에서 시작
FROM openjdk:21-jdk-slim

# 애플리케이션 JAR 파일을 복사
COPY build/libs/webty-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션 포트를 설정
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
