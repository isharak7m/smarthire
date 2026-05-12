FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
COPY frontend ../frontend

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/smarthire-backend-1.0.0.jar app.jar

EXPOSE 10000

CMD ["java", "-Dserver.port=10000", "-jar", "app.jar"]
