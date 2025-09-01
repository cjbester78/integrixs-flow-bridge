#!/bin/bash

# Fix enum type mismatches in adapter files

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Fixing enum type mismatches in adapter files..."

# First, add import for AdapterTypeConverter to all adapter files
for file in "$ADAPTERS_DIR"/*.java; do
    if [ -f "$file" ]; then
        # Check if AdapterException is used but AdapterTypeConverter is not imported
        if grep -q "AdapterException" "$file" && ! grep -q "import com.integrixs.adapters.infrastructure.util.AdapterTypeConverter" "$file"; then
            echo "Adding AdapterTypeConverter import to $(basename "$file")"
            
            # Add import after other imports
            sed -i '' '/^import.*adapters/a\
import com.integrixs.adapters.infrastructure.util.AdapterTypeConverter;
' "$file"
        fi
    fi
done

# Now fix the AdapterException constructor calls
echo "Fixing AdapterException constructor calls..."

for file in "$ADAPTERS_DIR"/*.java; do
    if [ -f "$file" ]; then
        # Replace AdapterConfiguration.AdapterTypeEnum.X with AdapterTypeConverter.toCoreType(AdapterConfiguration.AdapterTypeEnum.X)
        if grep -q "new AdapterException.*AdapterConfiguration.AdapterTypeEnum" "$file"; then
            echo "Fixing AdapterException calls in $(basename "$file")"
            
            # Fix patterns like: new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
            sed -i '' 's/new AdapterException\.\([A-Za-z]*\)Exception(\s*AdapterConfiguration\.AdapterTypeEnum\.\([A-Z]*\)/new AdapterException.\1Exception(AdapterTypeConverter.toCoreType(AdapterConfiguration.AdapterTypeEnum.\2)/g' "$file"
            
            # Fix patterns like: throw new AdapterException.OperationException(AdapterConfiguration.AdapterTypeEnum.JMS,
            sed -i '' 's/throw new AdapterException\.\([A-Za-z]*\)Exception(\s*AdapterConfiguration\.AdapterTypeEnum\.\([A-Z]*\)/throw new AdapterException.\1Exception(AdapterTypeConverter.toCoreType(AdapterConfiguration.AdapterTypeEnum.\2)/g' "$file"
        fi
    fi
done

echo "Done fixing enum mismatches!"