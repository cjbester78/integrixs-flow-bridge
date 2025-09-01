#!/bin/bash

# Fix the return types for getAdapterType() and getAdapterMode()

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Fixing adapter type returns..."

# Fix getAdapterType() return statements
find "$ADAPTERS_DIR" -name "*.java" -exec sed -i '' 's/return AdapterConfiguration\.AdapterTypeEnum\.\([A-Z]*\);/return AdapterType.\1;/g' {} \;

echo "Script completed!"