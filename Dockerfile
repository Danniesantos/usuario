FROM gradle:8.5.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar  /app/usuario.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/usuario.jar"]

