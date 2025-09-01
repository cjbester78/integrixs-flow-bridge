#!/bin/bash

cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter

# Fix all files with the broken test syntax
for file in *Adapter.java; do
    if [ -f "$file" ]; then
        echo "Fixing test syntax in $file..."
        
        # Fix the broken syntax: testResults.add( try { -> proper method call
        perl -i -pe 's/testResults\.add\(\s*try\s*\{/testResults.add(/g' "$file"
        
        # Fix the ending: ); -> proper closing
        perl -i -pe 's/\}\s*\)\s*;/);/g' "$file"
        
        # Remove duplicate imports
        awk '!seen[$0]++' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
done

echo "Test syntax fixed!"