FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DcreateDependencyReducedPom=false package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/buffet-man-1.0-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
