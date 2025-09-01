#!/bin/bash

# Script to add missing SenderAdapterPort interface methods to sender adapters

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# List of sender adapters
SENDER_ADAPTERS=(
    "HttpSenderAdapter.java"
    "JdbcSenderAdapter.java" 
    "RestSenderAdapter.java"
    "SoapSenderAdapter.java"
    "FileSenderAdapter.java"
    "MailSenderAdapter.java"
    "FtpSenderAdapter.java"
    "SftpSenderAdapter.java"
    "RfcSenderAdapter.java"
    "IdocSenderAdapter.java"
    "JmsSenderAdapter.java"
    "OdataSenderAdapter.java"
    "KafkaSenderAdapter.java"
)

for adapter in "${SENDER_ADAPTERS[@]}"; do
    file="$ADAPTERS_DIR/$adapter"
    if [ -f "$file" ]; then
        echo "Processing $adapter..."
        
        # Check if methods already exist
        if ! grep -q "void startListening" "$file"; then
            echo "Adding startListening, stopListening, isListening methods to $adapter..."
            
            # Find the last closing brace and insert methods before it
            # Using a more complex approach to handle the insertion properly
            
            # Create temporary file with the new methods
            cat > /tmp/sender_methods.txt << 'EOF'
    @Override
    public void startListening(SenderAdapterPort.DataReceivedCallback callback) {
        // Not implemented for this adapter type
        throw new UnsupportedOperationException("Push-based listening not supported by this adapter");
    }
    
    @Override
    public void stopListening() {
        // Not implemented for this adapter type
    }
    
    @Override
    public boolean isListening() {
        return false;
    }

}
EOF
            
            # Remove the last closing brace and append our methods
            sed -i '' -e '$d' "$file"
            cat /tmp/sender_methods.txt >> "$file"
            
            # Clean up
            rm /tmp/sender_methods.txt
        else
            echo "$adapter already has the required methods"
        fi
    fi
done

echo "Done adding SenderAdapterPort methods!"