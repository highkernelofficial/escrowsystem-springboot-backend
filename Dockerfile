# ── Stage 1: Build ──────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven wrapper & pom first (layer‑cached dependency download)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR (skip tests for faster builds)
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Run ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot default port
EXPOSE 8080

# JVM tuning flags (container‑friendly defaults)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
