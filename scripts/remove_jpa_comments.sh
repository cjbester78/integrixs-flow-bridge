#!/bin/bash

# Script to remove JPA-related comments from repository files
echo "🧹 Removing JPA comments from repository files..."

# Find all repository files with JPA comments
FILES=$(grep -l "JPA" /Users/cjbester/git/Integrixs-Flow-Bridge/data-access/src/main/java/com/integrixs/data/sql/repository/*.java)

for file in $FILES; do
    echo "🔧 Processing: $file"
    
    # Replace JPA comments with native SQL comments
    sed -i '' 's/without JPA overhead/using native SQL/g' "$file"
    
    echo "   ✅ Updated JPA comments in $file"
done

# Also fix the InMemoryAdapterRepository comment
ADAPTER_FILE="/Users/cjbester/git/Integrixs-Flow-Bridge/adapters/src/main/java/com/integrixs/adapters/infrastructure/persistence/InMemoryAdapterRepository.java"
if [ -f "$ADAPTER_FILE" ]; then
    echo "🔧 Processing: $ADAPTER_FILE"
    sed -i '' 's/Can be replaced with JPA implementation for database persistence/Can be replaced with SQL repository for database persistence/g' "$ADAPTER_FILE"
    echo "   ✅ Updated JPA comment in $ADAPTER_FILE"
fi

echo "✅ JPA comment removal completed!"