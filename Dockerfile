# Base image (OpenJDK 17)
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 파일 복사
COPY . /app

# Gradle 빌드 실행
RUN ./gradlew bootJar

# 실제 애플리케이션 이미지 생성
FROM openjdk:17-jdk-slim

# 환경 변수 설정
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Gradle 빌드 결과물 JAR 파일 경로 직접 지정
COPY --from=builder /app/build/libs/app.jar /app.jar

# 80번 포트 열기
EXPOSE 80

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
