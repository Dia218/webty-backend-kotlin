# OpenJDK 이미지에서 시작
FROM openjdk:21-jdk-slim AS build

# 프로젝트에 있는 Gradle Wrapper로 빌드
WORKDIR /app

# Gradle Wrapper 파일과 설정 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle.kts /app/
COPY settings.gradle.kts /app/

# 의존성 설치
RUN ./gradlew dependencies --no-daemon

# 애플리케이션 소스 코드 복사
COPY src /app/src

# 의존성 빌드 (테스트를 건너뛰고 빌드)
RUN ./gradlew build --no-daemon -x test

# 새로운 이미지 단계 (애플리케이션 실행)
FROM openjdk:21-jdk-slim

# 첫 번째 단계에서 빌드한 JAR 파일을 컨테이너로 복사
COPY --from=build /app/build/libs/webty-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션 포트 설정
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
