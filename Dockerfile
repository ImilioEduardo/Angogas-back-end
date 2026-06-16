# ── Fase 1: compilar o JAR ───────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Copiar apenas o pom.xml primeiro para aproveitar a cache do Docker
COPY pom.xml .
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn

# Descarregar dependências (camada separada — raramente muda)
RUN ./mvnw dependency:go-offline --no-transfer-progress -q

# Copiar código fonte e compilar
COPY src src
RUN ./mvnw package -DskipTests --no-transfer-progress -q

# ── Fase 2: imagem de runtime (mínima e segura) ──────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Utilizador sem privilégios
RUN addgroup -S angogas && adduser -S angogas -G angogas
USER angogas

# Copiar apenas o JAR da fase de build
COPY --from=build /build/target/backend-*.jar app.jar

EXPOSE 8080

# JVM flags para containers: sem saída de GC, heap adaptativo
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
