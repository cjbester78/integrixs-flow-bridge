#!/bin/bash

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home)

# Set log path from environment variable or use default
LOG_PATH=${INTEGRIX_LOG_PATH:-/Users/cjbester/git/Integrixs-Flow-Bridge/logs}

# Create logs directory structure if it doesn't exist
mkdir -p "$LOG_PATH/application"
mkdir -p "$LOG_PATH/startup"
mkdir -p "$LOG_PATH/structured"
mkdir -p "$LOG_PATH/security"
mkdir -p "$LOG_PATH/deployment"
mkdir -p "$LOG_PATH/adapter"
mkdir -p "$LOG_PATH/error"
mkdir -p "$LOG_PATH/frontend"

# Function to log with timestamp
log_with_timestamp() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_PATH/startup/startup-$(date '+%Y-%m-%d').log"
}

log_with_timestamp "🚀 Starting Integrix Flow Bridge application..."

# Check if already running
if [ -f "$LOG_PATH/backend.pid" ]; then
    EXISTING_PID=$(cat "$LOG_PATH/backend.pid")
    if ps -p $EXISTING_PID > /dev/null 2>&1; then
        log_with_timestamp "⚠️  Application is already running (PID: $EXISTING_PID)"
        log_with_timestamp "📌 Use stopapp.sh to stop the application first"
        exit 1
    else
        log_with_timestamp "🧹 Removing stale PID file"
        rm -f "$LOG_PATH/backend.pid"
    fi
fi

# Start backend
log_with_timestamp "🚀 Starting backend application..."
cd ../backend
nohup mvn spring-boot:run -Dspring-boot.run.arguments="--spring.main.allow-circular-references=true --logging.path=$LOG_PATH" > "$LOG_PATH/app.log" 2>&1 &

# Store the PID for later reference
BACKEND_PID=$!
echo $BACKEND_PID > "$LOG_PATH/backend.pid"

log_with_timestamp "✅ Application started successfully!"
log_with_timestamp "📌 Backend PID: $BACKEND_PID"
log_with_timestamp "📌 Application running on http://localhost:8080"
log_with_timestamp "📌 Check $LOG_PATH/app.log for application startup output"
log_with_timestamp "📌 Check $LOG_PATH/application/application-$(date '+%Y-%m-%d').log for application logs"
log_with_timestamp "📌 Check $LOG_PATH/error/error-$(date '+%Y-%m-%d').log for error details"
log_with_timestamp "📌 Use stopapp.sh to stop the application"