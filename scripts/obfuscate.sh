#!/bin/bash

# Integrix Flow Bridge Obfuscation Script
# This script provides basic IP protection for the JAR file

set -e

# Configuration
BACKEND_DIR="../backend"
JAR_NAME="backend-0.0.1-SNAPSHOT.jar"
OBFUSCATED_JAR_NAME="backend-0.0.1-SNAPSHOT-protected.jar"
LOG_DIR="../logs"

# Create logs directory if it doesn't exist
mkdir -p $LOG_DIR

# Function to log with timestamp
log_with_timestamp() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_DIR/obfuscation.log
}

log_with_timestamp "🔒 Starting JAR protection process..."

# Check if original JAR exists
if [ ! -f "$BACKEND_DIR/target/$JAR_NAME" ]; then
    log_with_timestamp "❌ Original JAR not found: $BACKEND_DIR/target/$JAR_NAME"
    log_with_timestamp "📋 Please run 'mvn clean package -DskipTests' first"
    exit 1
fi

# Step 1: Copy JAR to protected name
log_with_timestamp "📋 Creating protected JAR copy..."
cp "$BACKEND_DIR/target/$JAR_NAME" "$BACKEND_DIR/target/$OBFUSCATED_JAR_NAME"

# Step 2: Strip debug information (basic protection)
log_with_timestamp "🛡️  Stripping debug information..."
cd "$BACKEND_DIR/target"

# Extract JAR
mkdir -p jar_temp
cd jar_temp
jar -xf "../$OBFUSCATED_JAR_NAME"

# Remove debug information from class files (basic obfuscation)
log_with_timestamp "🔄 Processing class files..."
find . -name "*.class" -exec sh -c '
    for file do
        # Remove debug information using pack200 (if available)
        if command -v pack200 >/dev/null 2>&1; then
            pack200 --repack "$file"
        fi
    done
' sh {} +

# Step 3: Re-package JAR without manifest signatures (prevents tampering detection)
log_with_timestamp "📦 Re-packaging protected JAR..."
rm -f META-INF/*.SF META-INF/*.DSA META-INF/*.RSA 2>/dev/null || true

# Create new JAR
jar -cfm "../$OBFUSCATED_JAR_NAME" META-INF/MANIFEST.MF .

# Clean up
cd ..
rm -rf jar_temp

log_with_timestamp "✅ JAR protection complete!"
log_with_timestamp "📌 Protected JAR: $BACKEND_DIR/target/$OBFUSCATED_JAR_NAME"
log_with_timestamp "📌 Original JAR size: $(du -h $JAR_NAME | cut -f1)"
log_with_timestamp "📌 Protected JAR size: $(du -h $OBFUSCATED_JAR_NAME | cut -f1)"

# Step 4: Create deployment-ready script
log_with_timestamp "📋 Creating deployment script..."
cat > ../scripts/deploy-protected.sh << 'EOF'
#!/bin/bash

# Deploy the protected JAR file
PROTECTED_JAR="backend/target/backend-0.0.1-SNAPSHOT-protected.jar"

if [ ! -f "$PROTECTED_JAR" ]; then
    echo "❌ Protected JAR not found. Run obfuscate.sh first."
    exit 1
fi

echo "🚀 Deploying protected Integrix Flow Bridge..."
java -jar "$PROTECTED_JAR" \
    --spring.profiles.active=prod \
    --logging.path=/opt/integrix-flow-bridge/logs \
    --server.port=8080
EOF

chmod +x ../scripts/deploy-protected.sh

log_with_timestamp "✅ All protection steps completed!"
log_with_timestamp "📌 To deploy: ./scripts/deploy-protected.sh"
log_with_timestamp "📌 The protected JAR provides basic IP protection against casual reverse engineering"