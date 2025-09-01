#!/bin/bash

cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter

echo "Fixing syntax errors in infrastructure adapters..."

# Fix FTP adapters - they have missing closing braces and syntax errors
echo "Fixing FtpSenderAdapter..."
cat > FtpSenderAdapter.java.fix << 'EOF'
    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started successfully");
    }
    
    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped successfully");
    }
    
    @Override
    protected AdapterOperationResult performConnectionTest() {
        FTPClient testClient = null;
        try {
            testClient = createFtpClient();
            connectClient(testClient);
            
            if (!testClient.isConnected() || !FTPReply.isPositiveCompletion(testClient.getReplyCode())) {
                return AdapterOperationResult.failure("FTP connection failed, reply: " + testClient.getReplyString());
            }
            
            // Test directory access
            boolean dirExists = testClient.changeWorkingDirectory(sourceDirectory);
            if (!dirExists) {
                return AdapterOperationResult.failure("Source directory does not exist or is not accessible: " + sourceDirectory);
            }
            
            // Test file listing
            FTPFile[] files = testClient.listFiles();
            int fileCount = files != null ? files.length : 0;
            
            return AdapterOperationResult.success("FTP connection successful, directory contains " + fileCount + " items");
            
        } catch (Exception e) {
            log.error("FTP connection test failed", e);
            return AdapterOperationResult.failure("Connection test failed: " + e.getMessage());
        } finally {
            if (testClient != null) {
                try {
                    testClient.disconnect();
                } catch (Exception ignored) {}
            }
        }
    }
EOF

# Find line numbers for method insertion points
line_num=$(grep -n "protected AdapterOperationResult performShutdown" FtpSenderAdapter.java | cut -d: -f1)
if [ ! -z "$line_num" ]; then
    # Insert after the closing brace of performShutdown
    next_line=$((line_num + 6))
    sed -i '' "${next_line}r FtpSenderAdapter.java.fix" FtpSenderAdapter.java
fi

# Fix missing @Override annotations and missing braces
for file in *.java; do
    echo "Fixing $file..."
    
    # Fix missing @Override annotations
    sed -i '' 's/^\s*protected AdapterOperationResult perform/@Override\n    protected AdapterOperationResult perform/g' "$file"
    sed -i '' 's/^\s*public AdapterOperationResult fetch/@Override\n    public AdapterOperationResult fetch/g' "$file"
    sed -i '' 's/^\s*public AdapterOperationResult send/@Override\n    public AdapterOperationResult send/g' "$file"
    sed -i '' 's/^\s*public void startPolling/@Override\n    public void startPolling/g' "$file"
    sed -i '' 's/^\s*public void stopPolling/@Override\n    public void stopPolling/g' "$file"
    sed -i '' 's/^\s*public void registerDataCallback/@Override\n    public void registerDataCallback/g' "$file"
    sed -i '' 's/^\s*public AdapterMetadata getMetadata/@Override\n    public AdapterMetadata getMetadata/g' "$file"
    
    # Fix duplicate @Override
    sed -i '' 'N;/^\s*@Override\n\s*@Override$/s/@Override\n//' "$file"
    
    # Fix logger references
    sed -i '' 's/logger\./log\./g' "$file"
    
    # Fix missing return statements
    perl -i -pe 's/(protected AdapterOperationResult performInitialization[^{]*\{[^}]*logger\.info\([^)]+\);)(\s*})/$1\n        return AdapterOperationResult.success("Initialized successfully");$2/g' "$file"
    perl -i -pe 's/(protected AdapterOperationResult performShutdown[^{]*\{[^}]*logger\.info\([^)]+\);)(\s*})/$1\n        return AdapterOperationResult.success("Shutdown successfully");$2/g' "$file"
done

# Fix specific compilation errors for test results
echo "Fixing test result compilation errors..."
for file in FileSenderAdapter.java FileReceiverAdapter.java JdbcSenderAdapter.java JdbcReceiverAdapter.java \
            IdocSenderAdapter.java IdocReceiverAdapter.java JmsSenderAdapter.java JmsReceiverAdapter.java \
            SoapSenderAdapter.java SoapReceiverAdapter.java; do
    if [ -f "$file" ]; then
        echo "Fixing test results in $file..."
        
        # Fix the broken test syntax
        perl -i -pe 's/testResults\.(put|add)\([^,]+,\s*try\s*\{/testResults.add(/g' "$file"
        
        # Fix List<AdapterResult> to List<AdapterOperationResult>
        sed -i '' 's/List<AdapterResult>/List<AdapterOperationResult>/g' "$file"
        
        # Fix new AdapterResult[0] to proper syntax
        sed -i '' 's/testResults\.toArray\(new AdapterResult\[0\]\)/testResults/g' "$file"
    fi
done

# Fix Kafka adapters that have missing braces
echo "Fixing Kafka adapters..."
for file in KafkaSenderAdapter.java KafkaReceiverAdapter.java; do
    if [ -f "$file" ]; then
        # Fix missing closing braces
        echo "}" >> "$file"
    fi
done

# Fix FTP adapter specific issues
echo "Fixing FTP adapter issues..."
if [ -f "FtpSenderAdapter.java" ]; then
    # Fix missing closing braces in methods
    perl -i -pe 's/(\}\s*protected AdapterOperationResult perform)/$1\n    }\n\n    protected AdapterOperationResult perform/g' FtpSenderAdapter.java
    
    # Add missing closing brace at end if needed
    if ! tail -1 FtpSenderAdapter.java | grep -q '^}$'; then
        echo "}" >> FtpSenderAdapter.java
    fi
fi

# Clean up temporary files
rm -f *.fix

echo "Syntax error fixes completed!"