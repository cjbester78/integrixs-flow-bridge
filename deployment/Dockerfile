# Multi-stage Dockerfile for Integrix Flow Bridge

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy parent pom
COPY pom.xml .

# Copy module poms
COPY shared-lib/pom.xml ./shared-lib/
COPY data-access/pom.xml ./data-access/
COPY db/pom.xml ./db/
COPY adapters/pom.xml ./adapters/
COPY engine/pom.xml ./engine/
COPY monitoring/pom.xml ./monitoring/
COPY webclient/pom.xml ./webclient/
COPY webserver/pom.xml ./webserver/
COPY soap-bindings/pom.xml ./soap-bindings/
COPY backend/pom.xml ./backend/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY shared-lib/src ./shared-lib/src
COPY data-access/src ./data-access/src
COPY db/src ./db/src
COPY adapters/src ./adapters/src
COPY engine/src ./engine/src
COPY monitoring/src ./monitoring/src
COPY webclient/src ./webclient/src
COPY webserver/src ./webserver/src
COPY soap-bindings/src ./soap-bindings/src
COPY backend/src ./backend/src

# Build the application
RUN mvn clean package -DskipTests -P production

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app user
RUN addgroup -g 1000 integrix && \
    adduser -D -u 1000 -G integrix integrix

WORKDIR /app

# Copy the JAR from builder
COPY --from=builder /build/backend/target/backend-*.jar app.jar

# Copy static resources if any
COPY --from=builder /build/backend/target/classes/public ./public

# Create directories for logs and uploads
RUN mkdir -p /app/logs /app/uploads && \
    chown -R integrix:integrix /app

# Switch to non-root user
USER integrix

# Expose ports
EXPOSE 8080 8081

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx2048m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heap-dump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]