#!/bin/bash

# Find and remove duplicate method declarations

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Checking for duplicate methods in adapter files..."

for file in "$ADAPTERS_DIR"/*.java; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        
        # Count occurrences of each method
        metadata_count=$(grep -c "public AdapterMetadata getMetadata()" "$file" 2>/dev/null || echo 0)
        adapter_type_count=$(grep -c "protected AdapterType getAdapterType()" "$file" 2>/dev/null || echo 0)
        adapter_mode_count=$(grep -c "protected AdapterMode getAdapterMode()" "$file" 2>/dev/null || echo 0)
        
        if [ "$metadata_count" -gt 1 ] || [ "$adapter_type_count" -gt 1 ] || [ "$adapter_mode_count" -gt 1 ]; then
            echo "Found duplicates in $filename:"
            echo "  getMetadata(): $metadata_count"
            echo "  getAdapterType(): $adapter_type_count"
            echo "  getAdapterMode(): $adapter_mode_count"
        fi
        
        # Also check for methods that are already defined in parent
        if grep -q "method getMetadata() is already defined" "$file" 2>/dev/null; then
            echo "Error comment found in $filename about getMetadata()"
        fi
    fi
done

echo "Done checking for duplicates."