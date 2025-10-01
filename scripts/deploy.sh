#!/bin/bash

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home)

# Set log path from environment variable or use default
LOG_PATH=${INTEGRIX_LOG_PATH:-/Users/cjbester/git/Integrixs-Flow-Bridge/logs}

# Create logs directory if it doesn't exist
mkdir -p "$LOG_PATH/deployment"

# Function to log with timestamp
log_with_timestamp() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_PATH/deployment/deployment-$(date '+%Y-%m-%d').log"
}

log_with_timestamp "🚀 Starting deployment process..."

# 1. Kill all backend processes
log_with_timestamp "🛑 Stopping backend processes..."
pkill -f "spring-boot:run" || true
pkill -f "java.*backend" || true

# Wait a moment for processes to fully stop
sleep 2

# 2. Build the entire project
log_with_timestamp "🏗️  Building entire project..."
mvn clean install -DskipTests 2>&1 | tee -a "$LOG_PATH/deployment/deployment-$(date '+%Y-%m-%d').log"

if [ $? -ne 0 ]; then
    log_with_timestamp "❌ Project build failed!"
    exit 1
fi

# 3. Clean backend public directory
log_with_timestamp "📁 Cleaning backend public directory..."
rm -rf backend/src/main/resources/public/*
mkdir -p backend/src/main/resources/public

# 4. Build frontend
log_with_timestamp "🏗️  Building frontend..."
cd frontend
npm run build 2>&1 | tee -a "$LOG_PATH/deployment/deployment-$(date '+%Y-%m-%d').log"

# Check if frontend build was successful
if [ $? -ne 0 ]; then
    log_with_timestamp "❌ Frontend build failed!"
    exit 1
fi

# 5. Copy frontend to backend
log_with_timestamp "📋 Copying frontend to backend..."
cp -r dist/* ../backend/src/main/resources/public/

# 6. Start backend (development mode - no obfuscation)
log_with_timestamp "🚀 Starting backend in development mode..."
cd ../backend
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar \
    --spring.profiles.active=dev \
    --spring.main.allow-circular-references=true \
    --logging.path="$LOG_PATH" \
    > "$LOG_PATH/startup/startup-$(date '+%Y-%m-%d').log" 2>&1 &

# Store the PID for later reference
echo $! > "$LOG_PATH/backend.pid"

log_with_timestamp "✅ Deployment complete!"
log_with_timestamp "📌 Backend running on http://localhost:8080"
log_with_timestamp "📌 Check $LOG_PATH/startup/startup-$(date '+%Y-%m-%d').log for backend startup output"
log_with_timestamp "📌 Check $LOG_PATH/application/application-$(date '+%Y-%m-%d').log for application logs"
log_with_timestamp "📌 Check $LOG_PATH/error/error-$(date '+%Y-%m-%d').log for error details"
log_with_timestamp "📌 Backend PID stored in $LOG_PATH/backend.pid"