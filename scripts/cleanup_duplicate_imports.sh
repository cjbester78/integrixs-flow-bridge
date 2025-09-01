#!/bin/bash

# Remove duplicate imports from adapter files

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Cleaning up duplicate imports..."

for file in "$ADAPTERS_DIR"/*.java; do
    if [ -f "$file" ]; then
        # Create temp file with unique imports
        awk '
        /^import/ {
            if (!imports[$0]++) {
                print
            }
        }
        !/^import/ {
            print
        }
        ' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
done

echo "Done cleaning up imports!"