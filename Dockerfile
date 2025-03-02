# OpenJDK 이미지에서 시작
FROM openjdk:21-jdk-slim

# 프로젝트에 있는 Gradle Wrapper로 빌드
WORKDIR /app

# Gradle Wrapper 파일과 설정 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle.kts /app/
COPY settings.gradle.kts /app/

# 의존성 설치 및 빌드
RUN ./gradlew build --no-daemon

# 애플리케이션 소스 코드 복사
COPY src /app/src

# 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/webty-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션 포트 설정
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
