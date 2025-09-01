#!/bin/bash

# Script to remove @Override from performSend methods

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Removing @Override from performSend methods..."

# Find all files with @Override before performSend
find "$ADAPTERS_DIR" -name "*.java" -type f | while read file; do
    # Use perl for multiline replacement
    if grep -q "@Override.*performSend" "$file"; then
        echo "Processing $file..."
        perl -i -pe 's/\@Override\s*\n(\s*protected.*performSend)/$1/g' "$file"
    fi
done

echo "Done removing @Override from performSend methods!"