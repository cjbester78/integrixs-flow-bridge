#!/bin/bash

# Set log path from environment variable or use default
LOG_PATH=${INTEGRIX_LOG_PATH:-/Users/cjbester/git/Integrixs-Flow-Bridge/logs}

# Create logs directory if it doesn't exist
mkdir -p "$LOG_PATH/startup"

# Function to log with timestamp
log_with_timestamp() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_PATH/startup/shutdown-$(date '+%Y-%m-%d').log"
}

log_with_timestamp "🛑 Stopping Integrix Flow Bridge application..."

# Check if backend PID file exists
if [ -f "$LOG_PATH/backend.pid" ]; then
    BACKEND_PID=$(cat "$LOG_PATH/backend.pid")
    log_with_timestamp "📋 Found backend PID: $BACKEND_PID"
    
    # Check if process is still running
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        log_with_timestamp "🔄 Stopping backend process gracefully..."
        kill $BACKEND_PID
        
        # Wait for graceful shutdown
        log_with_timestamp "⏳ Waiting for graceful shutdown..."
        sleep 5
        
        # Force kill if still running
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            log_with_timestamp "⚠️  Forcing backend process termination..."
            kill -9 $BACKEND_PID
            sleep 2
        fi
        
        # Verify process is stopped
        if ps -p $BACKEND_PID > /dev/null 2>&1; then
            log_with_timestamp "❌ Failed to stop backend process"
            exit 1
        else
            log_with_timestamp "✅ Backend process stopped successfully"
        fi
    else
        log_with_timestamp "ℹ️  Backend process was not running"
    fi
    
    # Remove PID file
    rm -f "$LOG_PATH/backend.pid"
    log_with_timestamp "🧹 Removed PID file"
else
    log_with_timestamp "📄 No backend PID file found"
fi

# Kill any remaining Spring Boot processes as cleanup
log_with_timestamp "🧹 Cleaning up any remaining processes..."
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "java.*backend" 2>/dev/null || true

# Wait for processes to fully stop
sleep 2

log_with_timestamp "✅ Application stopped successfully!"
log_with_timestamp "📌 Check $LOG_PATH/startup/shutdown-$(date '+%Y-%m-%d').log for shutdown details"
log_with_timestamp "📌 Use startapp.sh to start the application"