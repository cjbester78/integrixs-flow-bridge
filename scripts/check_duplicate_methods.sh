#!/bin/bash

# Check all adapters for duplicate getMetadata methods
ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Checking for adapters with multiple getMetadata() methods..."

for file in "$ADAPTERS_DIR"/*.java; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        count=$(grep -c "getMetadata()" "$file")
        if [ "$count" -gt 1 ]; then
            echo "$filename has $count getMetadata() methods"
            grep -n "getMetadata()" "$file"
        fi
    fi
done