# Multi-stage build for optimal image size
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy pom files first for better caching
COPY pom.xml .
COPY shared-lib/pom.xml shared-lib/
COPY data-access/pom.xml data-access/
COPY db/pom.xml db/
COPY monitoring/pom.xml monitoring/
COPY adapters/pom.xml adapters/
COPY engine/pom.xml engine/
COPY webserver/pom.xml webserver/
COPY webclient/pom.xml webclient/
COPY soap-bindings/pom.xml soap-bindings/
COPY backend/pom.xml backend/

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY shared-lib/src shared-lib/src
COPY data-access/src data-access/src
COPY db/src db/src
COPY monitoring/src monitoring/src
COPY adapters/src adapters/src
COPY engine/src engine/src
COPY webserver/src webserver/src
COPY webclient/src webclient/src
COPY soap-bindings/src soap-bindings/src
COPY backend/src backend/src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install necessary packages
RUN apk add --no-cache \
    curl \
    bash \
    tzdata \
    && rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1000 integrix && \
    adduser -D -u 1000 -G integrix integrix

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/backend/target/backend-*.jar app.jar

# Copy the static frontend files if they exist
COPY --from=builder --chown=integrix:integrix /app/backend/src/main/resources/public/ /app/public/ 2>/dev/null || true

# Create necessary directories
RUN mkdir -p /app/logs /app/config /app/certs && \
    chown -R integrix:integrix /app

# Switch to non-root user
USER integrix

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+DisableExplicitGC \
    -Djava.security.egd=file:/dev/urandom \
    -Dspring.profiles.active=docker"

# Environment variables for configuration
ENV SERVER_PORT=8080 \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/integrixflowbridge \
    SPRING_DATASOURCE_USERNAME=integrix \
    SPRING_DATASOURCE_PASSWORD=integrix \
    INTEGRIX_MASTER_KEY=CHANGE_ME_IN_PRODUCTION \
    TZ=UTC

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]