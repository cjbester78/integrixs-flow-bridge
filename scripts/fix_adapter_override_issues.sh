#!/bin/bash

# Fix @Override annotation issues in adapter classes

echo "Fixing @Override annotation issues..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# 1. Fix FileReceiverAdapter
echo "Fixing FileReceiverAdapter..."
FILE="$ADAPTER_DIR/FileReceiverAdapter.java"
# Remove @Override from getPollingIntervalMs (line 623)
sed -i '' '623d' "$FILE"
# Fix performSend method call - change performSend(payload) to performSend(payload, headers)
sed -i '' '697s/return performSend(payload);/return performSend(payload, headers);/' "$FILE"
# Remove @Override from performSend(Object, Map) (line 694)
sed -i '' '693d' "$FILE"

# 2. Fix FileSystemAdapter
echo "Fixing FileSystemAdapter..."
FILE="$ADAPTER_DIR/FileSystemAdapter.java"
# Remove @Override from monitorDirectory (line 86)
sed -i '' '86d' "$FILE"
# Remove @Override from processFiles (line 217)
sed -i '' '217d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 461)
sed -i '' '461d' "$FILE"

# 3. Fix FtpReceiverAdapter
echo "Fixing FtpReceiverAdapter..."
FILE="$ADAPTER_DIR/FtpReceiverAdapter.java"
# Remove @Override from performSend(Object, Map) (line 571)
sed -i '' '571d' "$FILE"
# Fix performSend method call
sed -i '' 's/return performSend(payload);/return performSend(payload, headers);/g' "$FILE"

# 4. Fix FtpSenderAdapter
echo "Fixing FtpSenderAdapter..."
FILE="$ADAPTER_DIR/FtpSenderAdapter.java"
# Remove @Override from pollForNewData (line 85)
sed -i '' '85d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 525)
sed -i '' '525d' "$FILE"

# 5. Fix IdocReceiverAdapter
echo "Fixing IdocReceiverAdapter..."
FILE="$ADAPTER_DIR/IdocReceiverAdapter.java"
# Remove @Override from performSend(Object, Map) (line 539)
sed -i '' '539d' "$FILE"
# Fix performSend method call
sed -i '' 's/return performSend(payload);/return performSend(payload, headers);/g' "$FILE"

# 6. Fix IdocSenderAdapter
echo "Fixing IdocSenderAdapter..."
FILE="$ADAPTER_DIR/IdocSenderAdapter.java"
# Remove @Override from pollForRecords (line 84)
sed -i '' '84d' "$FILE"
# Remove @Override from extractRecords (line 256)
sed -i '' '256d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 491)
sed -i '' '491d' "$FILE"

# 7. Fix JdbcReceiverAdapter
echo "Fixing JdbcReceiverAdapter..."
FILE="$ADAPTER_DIR/JdbcReceiverAdapter.java"
# Remove @Override from performSend(Object, Map) (line 537)
sed -i '' '537d' "$FILE"

# 8. Fix JdbcSenderAdapter
echo "Fixing JdbcSenderAdapter..."
FILE="$ADAPTER_DIR/JdbcSenderAdapter.java"
# Remove @Override from pollData (line 77)
sed -i '' '77d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 623)
sed -i '' '623d' "$FILE"

# 9. Fix KafkaReceiverAdapter
echo "Fixing KafkaReceiverAdapter..."
FILE="$ADAPTER_DIR/KafkaReceiverAdapter.java"
# Remove @Override from performSend(Object, Map) (line 456)
sed -i '' '456d' "$FILE"
# Fix performSend method call
sed -i '' 's/return performSend(payload);/return performSend(payload, headers);/g' "$FILE"

# 10. Fix KafkaSenderAdapter
echo "Fixing KafkaSenderAdapter..."
FILE="$ADAPTER_DIR/KafkaSenderAdapter.java"
# Remove @Override from processMessage (line 78)
sed -i '' '78d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 440)
sed -i '' '440d' "$FILE"

# 11. Fix MailReceiverAdapter
echo "Fixing MailReceiverAdapter..."
FILE="$ADAPTER_DIR/MailReceiverAdapter.java"
# Remove @Override from performSend(Object, Map) (line 587)
sed -i '' '587d' "$FILE"
# Fix performSend method call
sed -i '' 's/return performSend(payload);/return performSend(payload, headers);/g' "$FILE"

# 12. Fix MailSenderAdapter
echo "Fixing MailSenderAdapter..."
FILE="$ADAPTER_DIR/MailSenderAdapter.java"
# Remove @Override from performReceive (line 77)
sed -i '' '77d' "$FILE"
# Remove multiple @Override annotations (lines 590, 596, 601, 606, 611)
for line in 611 606 601 596 590; do
    sed -i '' "${line}d" "$FILE"
done

# 13. Fix OdataReceiverAdapter
echo "Fixing OdataReceiverAdapter..."
FILE="$ADAPTER_DIR/OdataReceiverAdapter.java"
# Remove @Override from performReceive (line 78)
sed -i '' '78d' "$FILE"
# Remove @Override from performSend(Object) (line 84)
sed -i '' '84d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 355)
sed -i '' '355d' "$FILE"
# Remove @Override from getPollingIntervalMs (line 476)
sed -i '' '476d' "$FILE"

# 14. Fix OdataSenderAdapter
echo "Fixing OdataSenderAdapter..."
FILE="$ADAPTER_DIR/OdataSenderAdapter.java"
# Remove @Override from pollData (line 84)
sed -i '' '84d' "$FILE"
# Remove @Override from performSend(Object, Map) (line 250)
sed -i '' '250d' "$FILE"
# Remove multiple @Override annotations (lines 293, 299, 303)
for line in 303 299 293; do
    sed -i '' "${line}d" "$FILE"
done

echo "Done fixing @Override issues!"