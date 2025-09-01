#!/bin/bash

# Frontend Adapter Naming Refactoring Script
# Updates TypeScript/React files to use INBOUND/OUTBOUND terminology

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting Frontend Adapter Naming Refactoring...${NC}"

# Check if we're in the right directory
if [ ! -d "frontend" ]; then
    echo -e "${RED}Error: This script must be run from the project root directory${NC}"
    exit 1
fi

# Function to update file contents
update_contents() {
    local old_text=$1
    local new_text=$2
    local file_pattern=$3
    
    echo -e "${YELLOW}Updating content: $old_text → $new_text${NC}"
    
    # Use different sed syntax for macOS vs Linux
    if [[ "$OSTYPE" == "darwin"* ]]; then
        find frontend/src -name "$file_pattern" -type f -exec sed -i '' "s/${old_text}/${new_text}/g" {} +
    else
        find frontend/src -name "$file_pattern" -type f -exec sed -i "s/${old_text}/${new_text}/g" {} +
    fi
}

# Function to rename files
rename_files() {
    local old_pattern=$1
    local new_pattern=$2
    
    echo -e "${YELLOW}Renaming files: $old_pattern → $new_pattern${NC}"
    
    find frontend/src -name "*${old_pattern}*" -type f | while read -r file; do
        new_file=$(echo "$file" | sed "s/${old_pattern}/${new_pattern}/g")
        if [ "$file" != "$new_file" ]; then
            echo "  Renaming: $(basename "$file") → $(basename "$new_file")"
            mv "$file" "$new_file"
        fi
    done
}

echo -e "${GREEN}Step 1: Updating TypeScript type definitions...${NC}"
update_contents "SENDER" "INBOUND" "*.ts"
update_contents "RECEIVER" "OUTBOUND" "*.ts"
update_contents "'sender'" "'inbound'" "*.ts"
update_contents "'receiver'" "'outbound'" "*.ts"

echo -e "${GREEN}Step 2: Updating React components...${NC}"
update_contents "SENDER" "INBOUND" "*.tsx"
update_contents "RECEIVER" "OUTBOUND" "*.tsx"
update_contents "Sender" "Inbound" "*.tsx"
update_contents "Receiver" "Outbound" "*.tsx"

echo -e "${GREEN}Step 3: Updating adapter references...${NC}"
update_contents "sourceAdapter" "inboundAdapter" "*.tsx"
update_contents "targetAdapter" "outboundAdapter" "*.tsx"
update_contents "sourceAdapter" "inboundAdapter" "*.ts"
update_contents "targetAdapter" "outboundAdapter" "*.ts"
update_contents "SourceAdapter" "InboundAdapter" "*.tsx"
update_contents "TargetAdapter" "OutboundAdapter" "*.tsx"

echo -e "${GREEN}Step 4: Renaming component files...${NC}"
rename_files "SenderAdapter" "InboundAdapter"
rename_files "ReceiverAdapter" "OutboundAdapter"

echo -e "${GREEN}Step 5: Updating adapter mode labels...${NC}"
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Special handling for label updates
    find frontend/src -name "*.tsx" -type f -exec sed -i '' "s/'Sender (Inbound)'/'Inbound'/g" {} +
    find frontend/src -name "*.tsx" -type f -exec sed -i '' "s/'Receiver (Outbound)'/'Outbound'/g" {} +
else
    find frontend/src -name "*.tsx" -type f -exec sed -i "s/'Sender (Inbound)'/'Inbound'/g" {} +
    find frontend/src -name "*.tsx" -type f -exec sed -i "s/'Receiver (Outbound)'/'Outbound'/g" {} +
fi

echo -e "${GREEN}Frontend refactoring complete!${NC}"
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Review the changes: cd frontend && git diff"
echo "2. Run frontend build: npm run build"
echo "3. Run frontend tests: npm test"
echo "4. Test the UI thoroughly"