#!/bin/bash

# Fix missing imports in adapter files

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Fixing imports in adapter files..."

# Function to ensure Map import exists
ensure_map_import() {
    local file=$1
    
    # Check if Map is used but not imported
    if grep -q "Map<" "$file" && ! grep -q "import java.util.Map;" "$file"; then
        echo "Adding Map import to $(basename "$file")"
        
        # Find where to insert the import (after other java.util imports or after package)
        if grep -q "import java.util" "$file"; then
            # Add after last java.util import
            sed -i '' '/import java.util/{ 
                h
                :a
                n
                /import java.util/ba
                x
                a\
import java.util.Map;
                g
            }' "$file"
        else
            # Add after package declaration
            sed -i '' '/^package /a\
\
import java.util.Map;
' "$file"
        fi
    fi
}

# Process all adapter files
for file in "$ADAPTERS_DIR"/*.java; do
    if [ -f "$file" ]; then
        ensure_map_import "$file"
    fi
done

echo "Done fixing imports!"