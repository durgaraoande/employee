FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /employee
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests
FROM openjdk:17-jdk-slim
WORKDIR /employee
COPY --from=build /employee/target/employee-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/employee/employee-0.0.1-SNAPSHOT.jar"]