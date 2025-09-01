#!/bin/bash

# Script to fix performSend methods in sender adapters that call performReceive

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Check HttpSenderAdapter
echo "Checking HttpSenderAdapter..."
if grep -q "return performReceive(payload)" "$ADAPTERS_DIR/HttpSenderAdapter.java"; then
    echo "Fixing HttpSenderAdapter..."
    sed -i '' 's/return performReceive(payload);/return pollForData();/g' "$ADAPTERS_DIR/HttpSenderAdapter.java"
fi

echo "Done fixing performSend methods!"