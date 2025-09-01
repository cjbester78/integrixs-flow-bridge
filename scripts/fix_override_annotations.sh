#!/bin/bash

# Script to remove @Override annotations from methods that are not in the interface

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Methods that should NOT have @Override (not in SenderAdapterPort interface)
METHODS_TO_FIX=(
    "registerDataCallback"
    "startPolling"
    "stopPolling"
)

echo "Removing incorrect @Override annotations..."

for method in "${METHODS_TO_FIX[@]}"; do
    echo "Fixing @Override for $method methods..."
    
    # Find files with @Override before these methods and remove the annotation
    find "$ADAPTERS_DIR" -name "*.java" -type f -exec grep -l "@Override.*\n.*public.*$method" {} \; | while read file; do
        echo "Processing $file for method $method..."
        
        # Use perl for multiline replacement
        perl -i -pe 's/\@Override\s*\n(\s*public.*'"$method"')/$1/g' "$file"
    done
done

# Also fix performReceive and performSend methods that have incorrect @Override
echo "Fixing @Override for performReceive methods in Receiver adapters..."
for file in "$ADAPTERS_DIR"/*ReceiverAdapter.java; do
    if [ -f "$file" ]; then
        # Remove @Override from performReceive in ReceiverAdapter classes
        perl -i -pe 's/\@Override\s*\n(\s*protected.*performReceive)/$1/g' "$file"
    fi
done

echo "Fixing @Override for performSend methods in Sender adapters..."
for file in "$ADAPTERS_DIR"/*SenderAdapter.java; do
    if [ -f "$file" ]; then
        # Remove @Override from performReceive in SenderAdapter classes (they use performReceive internally)
        perl -i -pe 's/\@Override\s*\n(\s*protected.*performReceive)/$1/g' "$file"
    fi
done

# Fix specific methods in specific files
echo "Fixing other incorrect @Override annotations..."

# Fix supportsBatchOperations and getMaxBatchSize methods
find "$ADAPTERS_DIR" -name "*.java" -type f -exec grep -l "@Override.*\n.*public.*supportsBatchOperations\|@Override.*\n.*public.*getMaxBatchSize" {} \; | while read file; do
    echo "Processing $file for batch methods..."
    perl -i -pe 's/\@Override\s*\n(\s*public.*supportsBatchOperations)/$1/g' "$file"
    perl -i -pe 's/\@Override\s*\n(\s*public.*getMaxBatchSize)/$1/g' "$file"
done

echo "Done fixing @Override annotations!"