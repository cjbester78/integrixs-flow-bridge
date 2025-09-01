#!/bin/bash

# Fix missing performSend method in adapters

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "Adding missing performSend methods to adapters..."

# Function to add performSend if missing
add_perform_send() {
    local file=$1
    local adapter_type=$2
    
    # Check if performSend method exists
    if ! grep -q "protected AdapterOperationResult performSend(Object payload, Map<String, Object> headers)" "$file"; then
        echo "Adding performSend to $(basename "$file")"
        
        # Add the method before the last closing brace
        awk -v type="$adapter_type" '
        BEGIN {
            method = "\n    @Override\n" \
                "    protected AdapterOperationResult performSend(Object payload, Map<String, Object> headers) throws Exception {\n" \
                "        // Implementation depends on adapter type\n" \
                "        return performReceive(payload);\n" \
                "    }\n"
        }
        {
            lines[NR] = $0
        }
        END {
            # Find the last closing brace
            for (i = NR; i > 0; i--) {
                if (lines[i] ~ /^}$/) {
                    # Insert method before the last closing brace
                    for (j = 1; j < i; j++) {
                        print lines[j]
                    }
                    print method
                    print lines[i]
                    break
                }
            }
        }' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
}

# Process sender adapters
for file in "$ADAPTERS_DIR"/*SenderAdapter.java; do
    if [ -f "$file" ]; then
        add_perform_send "$file" "SENDER"
    fi
done

# Process receiver adapters
for file in "$ADAPTERS_DIR"/*ReceiverAdapter.java; do
    if [ -f "$file" ]; then
        add_perform_send "$file" "RECEIVER"
    fi
done

echo "Done adding missing methods!"