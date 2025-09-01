#!/bin/bash

# Script to fix all adapter implementations

echo "Fixing adapter implementations..."

# Function to add the required methods to an adapter
add_adapter_methods() {
    local file=$1
    local adapter_type=$2
    local adapter_mode=$3
    local description=$4
    
    # Extract the class name from the file
    local class_name=$(basename "$file" .java)
    
    echo "Processing $class_name..."
    
    # Check if getMetadata() already exists
    if grep -q "public AdapterMetadata getMetadata()" "$file"; then
        echo "  - getMetadata() already exists, updating..."
        # Update the existing getMetadata() method
        sed -i '' "s/\.adapterType(.*)/\.adapterType(AdapterConfiguration.AdapterTypeEnum.$adapter_type)/" "$file"
        sed -i '' "s/\.adapterMode(.*SENDER.*)/\.adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)/" "$file"
        sed -i '' "s/\.adapterMode(.*RECEIVER.*)/\.adapterMode(AdapterConfiguration.AdapterModeEnum.RECEIVER)/" "$file"
    else
        echo "  - Adding getMetadata() method..."
        # Find the last closing brace and insert before it
        # Using awk to insert the methods before the final closing brace
        awk -v type="$adapter_type" -v mode="$adapter_mode" -v desc="$description" '
        BEGIN {
            methods = "\n    @Override\n" \
                "    public AdapterMetadata getMetadata() {\n" \
                "        return AdapterMetadata.builder()\n" \
                "                .adapterType(AdapterConfiguration.AdapterTypeEnum." type ")\n" \
                "                .adapterMode(AdapterConfiguration.AdapterModeEnum." mode ")\n" \
                "                .description(\"" desc "\")\n" \
                "                .version(\"1.0.0\")\n" \
                "                .supportsBatch(true)\n" \
                "                .supportsAsync(true)\n" \
                "                .build();\n" \
                "    }\n"
        }
        {
            lines[NR] = $0
        }
        END {
            # Find the last closing brace
            for (i = NR; i > 0; i--) {
                if (lines[i] ~ /^}$/) {
                    # Insert methods before the last closing brace
                    for (j = 1; j < i; j++) {
                        print lines[j]
                    }
                    print methods
                    print lines[i]
                    break
                }
            }
        }' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
    
    # Check if getAdapterType() exists
    if ! grep -q "protected AdapterType getAdapterType()" "$file"; then
        echo "  - Adding getAdapterType() and getAdapterMode() methods..."
        # Add the abstract method implementations
        awk -v type="$adapter_type" -v mode="$adapter_mode" '
        BEGIN {
            methods = "\n    @Override\n" \
                "    protected AdapterType getAdapterType() {\n" \
                "        return AdapterType." type ";\n" \
                "    }\n" \
                "\n" \
                "    @Override\n" \
                "    protected AdapterMode getAdapterMode() {\n" \
                "        return AdapterMode." mode ";\n" \
                "    }\n"
        }
        {
            lines[NR] = $0
        }
        END {
            # Find the last closing brace
            for (i = NR; i > 0; i--) {
                if (lines[i] ~ /^}$/) {
                    # Insert methods before the last closing brace
                    for (j = 1; j < i; j++) {
                        print lines[j]
                    }
                    print methods
                    print lines[i]
                    break
                }
            }
        }' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
    fi
}

# Define the adapters directory
ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Process each adapter
# Format: add_adapter_methods "file" "ADAPTER_TYPE" "MODE" "description"

# Sender adapters (receive FROM external systems)
add_adapter_methods "$ADAPTERS_DIR/FtpSenderAdapter.java" "FTP" "SENDER" "FTP Sender adapter - receives files from FTP servers"
add_adapter_methods "$ADAPTERS_DIR/HttpSenderAdapter.java" "HTTP" "SENDER" "HTTP Sender adapter - receives data via HTTP endpoints"
add_adapter_methods "$ADAPTERS_DIR/IdocSenderAdapter.java" "IDOC" "SENDER" "IDoc Sender adapter - receives IDocs from SAP systems"
add_adapter_methods "$ADAPTERS_DIR/JdbcSenderAdapter.java" "JDBC" "SENDER" "JDBC Sender adapter - polls data from databases"
add_adapter_methods "$ADAPTERS_DIR/JmsSenderAdapter.java" "JMS" "SENDER" "JMS Sender adapter - receives messages from JMS queues/topics"
add_adapter_methods "$ADAPTERS_DIR/KafkaSenderAdapter.java" "KAFKA" "SENDER" "Kafka Sender adapter - consumes messages from Kafka topics"
add_adapter_methods "$ADAPTERS_DIR/OdataSenderAdapter.java" "ODATA" "SENDER" "OData Sender adapter - receives data from OData services"
add_adapter_methods "$ADAPTERS_DIR/SftpSenderAdapter.java" "SFTP" "SENDER" "SFTP Sender adapter - receives files from SFTP servers"
add_adapter_methods "$ADAPTERS_DIR/SoapSenderAdapter.java" "SOAP" "SENDER" "SOAP Sender adapter - receives SOAP requests"

# Receiver adapters (send TO external systems)
add_adapter_methods "$ADAPTERS_DIR/FtpReceiverAdapter.java" "FTP" "RECEIVER" "FTP Receiver adapter - sends files to FTP servers"
add_adapter_methods "$ADAPTERS_DIR/HttpReceiverAdapter.java" "HTTP" "RECEIVER" "HTTP Receiver adapter - sends data to HTTP endpoints"
add_adapter_methods "$ADAPTERS_DIR/IdocReceiverAdapter.java" "IDOC" "RECEIVER" "IDoc Receiver adapter - sends IDocs to SAP systems"
add_adapter_methods "$ADAPTERS_DIR/JdbcReceiverAdapter.java" "JDBC" "RECEIVER" "JDBC Receiver adapter - writes data to databases"
add_adapter_methods "$ADAPTERS_DIR/JmsReceiverAdapter.java" "JMS" "RECEIVER" "JMS Receiver adapter - sends messages to JMS destinations"
add_adapter_methods "$ADAPTERS_DIR/KafkaReceiverAdapter.java" "KAFKA" "RECEIVER" "Kafka Receiver adapter - produces messages to Kafka topics"
add_adapter_methods "$ADAPTERS_DIR/OdataReceiverAdapter.java" "ODATA" "RECEIVER" "OData Receiver adapter - sends data to OData services"
add_adapter_methods "$ADAPTERS_DIR/SftpReceiverAdapter.java" "SFTP" "RECEIVER" "SFTP Receiver adapter - sends files to SFTP servers"
add_adapter_methods "$ADAPTERS_DIR/SoapReceiverAdapter.java" "SOAP" "RECEIVER" "SOAP Receiver adapter - sends SOAP requests"

echo "Done processing adapters!"

# Also fix AdapterConfiguration.AdapterTypeEnum usage in other files
echo "Fixing AdapterConfiguration.AdapterTypeEnum usage..."

# Fix AdapterType usage to AdapterConfiguration.AdapterTypeEnum
find "$ADAPTERS_DIR" -name "*.java" -exec sed -i '' 's/AdapterType\./AdapterConfiguration.AdapterTypeEnum./g' {} \;

echo "Script completed!"