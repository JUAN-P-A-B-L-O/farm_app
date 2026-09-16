FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests package && cp .build/root/farmapp-*.jar /app/farmapp.jar

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system farmapp && useradd --system --gid farmapp farmapp
WORKDIR /app
COPY --from=build /app/farmapp.jar app.jar
USER farmapp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
