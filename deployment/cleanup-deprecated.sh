#!/bin/bash

# Cleanup script for removing deprecated code after refactoring
# Run this script after ensuring all functionality has been migrated

echo "========================================="
echo "Cleaning up deprecated code"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to remove file safely
remove_file() {
    local file=$1
    if [ -f "$file" ]; then
        echo -e "${YELLOW}Removing deprecated file: $file${NC}"
        rm -f "$file"
        echo -e "${GREEN}Removed: $file${NC}"
    else
        echo -e "${RED}File not found: $file${NC}"
    fi
}

# Function to remove directory safely
remove_directory() {
    local dir=$1
    if [ -d "$dir" ]; then
        echo -e "${YELLOW}Removing deprecated directory: $dir${NC}"
        rm -rf "$dir"
        echo -e "${GREEN}Removed: $dir${NC}"
    else
        echo -e "${RED}Directory not found: $dir${NC}"
    fi
}

# Change to project root
cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend

echo -e "${YELLOW}Step 1: Removing deprecated service packages${NC}"
remove_directory "backend/src/main/java/com/integrixs/backend/service/deprecated"

echo -e "${YELLOW}Step 2: Removing deprecated engine classes${NC}"
remove_file "engine/src/main/java/com/integrixs/engine/service/MessageProcessingEngine.java"
remove_file "engine/src/main/java/com/integrixs/engine/impl/AdapterExecutorImpl.java"

echo -e "${YELLOW}Step 3: Removing deprecated webserver classes${NC}"
remove_file "webserver/src/main/java/com/integrixs/webserver/legacy/IntegrationWebClient.java"

echo -e "${YELLOW}Step 4: Removing backward compatibility controllers (optional)${NC}"
echo "The following files provide backward compatibility and should only be removed after migration:"
echo "- webclient/src/main/java/com/integrixs/webclient/InboundRestController.java"
echo ""
read -p "Do you want to remove backward compatibility controllers? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    remove_file "webclient/src/main/java/com/integrixs/webclient/InboundRestController.java"
fi

echo -e "${YELLOW}Step 5: Cleaning up old test files${NC}"
find . -name "*Test.java" -path "*/deprecated/*" -type f -delete
find . -name "*IT.java" -path "*/deprecated/*" -type f -delete

echo -e "${YELLOW}Step 6: Removing empty packages${NC}"
find . -type d -empty -delete

echo -e "${YELLOW}Step 7: Checking for remaining @Deprecated annotations${NC}"
echo "Remaining deprecated code (review manually):"
grep -r "@Deprecated" --include="*.java" . | grep -v "target" | grep -v ".git" || echo "No deprecated annotations found"

echo -e "${GREEN}Cleanup completed!${NC}"
echo ""
echo "Next steps:"
echo "1. Run 'mvn clean compile' to ensure build still passes"
echo "2. Run tests to verify functionality"
echo "3. Commit the cleanup changes"