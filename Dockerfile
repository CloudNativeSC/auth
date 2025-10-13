# 1. JDK 21 기반 이미지 사용
FROM eclipse-temurin:21-jdk-jammy

# 2. 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 3. Gradle 빌드 결과 JAR 파일 복사
COPY ./build/libs/*.jar app.jar

# 4. 환경변수 설정 (docker-compose.yml에서 주입 가능)
ENV SERVER_PORT=8080

# 5. 컨테이너에서 노출할 포트 (모든 서비스 8080으로 통일)
EXPOSE ${SERVER_PORT}

# 6. 컨테이너 실행 시 JAR 실행
#    sh -c를 사용해서 환경변수를 읽도록 함
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${SERVER_PORT}"]
