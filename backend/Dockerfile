# Java 21 Temurin images provide a maintained, container-aware JDK/JRE pair.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system crosspay \
    && useradd --system --gid crosspay --home-dir /app crosspay
COPY --from=build --chown=crosspay:crosspay /workspace/target/*.jar /app/app.jar

USER crosspay
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 CMD ["curl", "--fail", "--silent", "http://localhost:8080/actuator/health"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
