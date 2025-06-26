FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN ./mvnw dependency:go-offline

COPY src/ /app/src/

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/TrackerBoost-0.0.1-SNAPSHOT.jar /app/Tracker-Boost.jar

COPY src/main/resources/application.properties config/application.properties

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "Tracker-Boost.jar", "--spring.config.location=file:/app/config/application.properties"]
