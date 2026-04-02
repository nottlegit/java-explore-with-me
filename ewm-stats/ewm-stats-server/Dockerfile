FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY ewm-stats ./ewm-stats
COPY checkstyle.xml ./

RUN mvn -q -pl ewm-stats/ewm-stats-server -am package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/ewm-stats/ewm-stats-server/target/ewm-stats-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
