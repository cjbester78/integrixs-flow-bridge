#!/bin/bash

# Script to fix final adapter compilation issues

ADAPTERS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

echo "=== Fixing final adapter compilation issues ==="

# 1. Fix FileReceiverAdapter performSend calls
echo "Fixing FileReceiverAdapter performSend calls..."
perl -i -pe 's/return performSend\(fileData\);/return performSend(fileData, new HashMap<>());/g' "$ADAPTERS_DIR/FileReceiverAdapter.java"

# 2. Fix OdataReceiverAdapter performSend calls  
echo "Fixing OdataReceiverAdapter performSend calls..."
perl -i -pe 's/return performSend\(payload\);/return performSend(payload, new HashMap<>());/g' "$ADAPTERS_DIR/OdataReceiverAdapter.java"

# 3. Fix FtpReceiverAdapter performSend calls
echo "Fixing FtpReceiverAdapter performSend calls..."
perl -i -pe 's/return performSend\(fileData\);/return performSend(fileData, new HashMap<>());/g' "$ADAPTERS_DIR/FtpReceiverAdapter.java"

# 4. Fix SftpReceiverAdapter performSend calls
echo "Fixing SftpReceiverAdapter performSend calls..."
perl -i -pe 's/return performSend\(fileData\);/return performSend(fileData, new HashMap<>());/g' "$ADAPTERS_DIR/SftpReceiverAdapter.java"

# 5. Add missing getAdapterType and getAdapterMode methods to REST adapters
echo "Adding missing methods to RestReceiverAdapter..."
cat >> "$ADAPTERS_DIR/RestReceiverAdapter.java" << 'EOF'
    
    @Override
    protected AdapterType getAdapterType() {
        return AdapterType.REST;
    }
    
    @Override
    protected AdapterMode getAdapterMode() {
        return AdapterMode.RECEIVER;
    }
}
EOF

# Remove the extra closing brace if it exists
perl -i -pe 's/\}\s*\n\s*\}$/\}/g' "$ADAPTERS_DIR/RestReceiverAdapter.java"

# 6. Fix JdbcSenderAdapter issues
echo "Fixing JdbcSenderAdapter..."
# Add missing abstract methods if not present
if ! grep -q "protected AdapterType getAdapterType()" "$ADAPTERS_DIR/JdbcSenderAdapter.java"; then
    perl -i -pe 's/(public class JdbcSenderAdapter.*\{)/$1\n\n    \@Override\n    protected AdapterType getAdapterType() {\n        return AdapterType.JDBC;\n    }\n\n    \@Override\n    protected AdapterMode getAdapterMode() {\n        return AdapterMode.SENDER;\n    }/g' "$ADAPTERS_DIR/JdbcSenderAdapter.java"
fi

# 7. Remove duplicate @Override annotations
echo "Removing duplicate @Override annotations..."
for file in "$ADAPTERS_DIR"/*.java; do
    # Remove multiple consecutive @Override annotations
    perl -i -pe 's/(\s*\@Override\s*\n)+(\s*\@Override)/$2/g' "$file"
done

# 8. Fix missing imports for HashMap
echo "Adding missing HashMap imports..."
for file in "$ADAPTERS_DIR"/*ReceiverAdapter.java; do
    if grep -q "new HashMap<>()" "$file" && ! grep -q "import java.util.HashMap" "$file"; then
        sed -i '' '/import java\.util\./a\
import java.util.HashMap;' "$file"
    fi
done

# 9. Fix IdocSenderAdapter missing methods
echo "Fixing IdocSenderAdapter..."
if ! grep -q "protected AdapterType getAdapterType()" "$ADAPTERS_DIR/IdocSenderAdapter.java"; then
    perl -i -pe 's/(public String getConfigurationSummary\(\).*?\})/$1\n\n    \@Override\n    protected AdapterType getAdapterType() {\n        return AdapterType.IDOC;\n    }\n\n    \@Override\n    protected AdapterMode getAdapterMode() {\n        return AdapterMode.SENDER;\n    }/gs' "$ADAPTERS_DIR/IdocSenderAdapter.java"
fi

# 10. Fix MailSenderAdapter duplicate methods
echo "Fixing MailSenderAdapter duplicate methods..."
# Remove duplicate getAdapterType and getAdapterMode if they exist
perl -i -pe 'BEGIN{undef $/;} s/(\@Override\s*protected AdapterType getAdapterType.*?\})\s*\n\s*\1/$1/smg' "$ADAPTERS_DIR/MailSenderAdapter.java"
perl -i -pe 'BEGIN{undef $/;} s/(\@Override\s*protected AdapterMode getAdapterMode.*?\})\s*\n\s*\1/$1/smg' "$ADAPTERS_DIR/MailSenderAdapter.java"

echo "=== Done fixing adapter issues ==="