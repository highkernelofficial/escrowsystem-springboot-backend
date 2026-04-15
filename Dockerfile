# ── Stage 1: Build ──────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven wrapper & pom first (layer-cached dependency download)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Fix Windows CRLF line endings & make executable
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR (skip tests for faster builds)
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Run ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR (use find to get the exact filename)
COPY --from=build /app/target/milestonebackend-0.0.1-SNAPSHOT.jar app.jar

# Render injects PORT at runtime
EXPOSE 8080

# JVM tuning flags (container-friendly + force IPv4 for Render)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.net.preferIPv4Stack=true"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
