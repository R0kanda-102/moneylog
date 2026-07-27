# ===== 1단계: 빌드 =====
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --version
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# ===== 2단계: 실행 =====
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]