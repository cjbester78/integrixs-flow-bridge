#!/bin/bash

# Adapter Naming Refactoring Script
# This script automates the renaming of adapter files and updates their content

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting Adapter Naming Refactoring...${NC}"

# Check if we're in the right directory
if [ ! -d "adapters" ] || [ ! -d "backend" ] || [ ! -d "frontend" ]; then
    echo -e "${RED}Error: This script must be run from the project root directory${NC}"
    exit 1
fi

# Create backup
echo -e "${YELLOW}Creating backup...${NC}"
BACKUP_DIR="backup_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"
cp -r adapters "$BACKUP_DIR/"
cp -r backend "$BACKUP_DIR/"
cp -r frontend "$BACKUP_DIR/"
echo -e "${GREEN}Backup created in $BACKUP_DIR${NC}"

# Function to rename files
rename_files() {
    local old_pattern=$1
    local new_pattern=$2
    local dir=$3
    
    echo -e "${YELLOW}Renaming files: $old_pattern → $new_pattern in $dir${NC}"
    
    find "$dir" -name "*${old_pattern}*" -type f | while read -r file; do
        new_file=$(echo "$file" | sed "s/${old_pattern}/${new_pattern}/g")
        if [ "$file" != "$new_file" ]; then
            echo "  Renaming: $(basename "$file") → $(basename "$new_file")"
            mkdir -p "$(dirname "$new_file")"
            mv "$file" "$new_file"
        fi
    done
}

# Function to update file contents
update_contents() {
    local old_text=$1
    local new_text=$2
    local file_pattern=$3
    
    echo -e "${YELLOW}Updating content: $old_text → $new_text${NC}"
    
    # Use different sed syntax for macOS vs Linux
    if [[ "$OSTYPE" == "darwin"* ]]; then
        find . -name "$file_pattern" -type f -not -path "./$BACKUP_DIR/*" -not -path "./.git/*" -not -path "./target/*" -not -path "./node_modules/*" -exec sed -i '' "s/${old_text}/${new_text}/g" {} +
    else
        find . -name "$file_pattern" -type f -not -path "./$BACKUP_DIR/*" -not -path "./.git/*" -not -path "./target/*" -not -path "./node_modules/*" -exec sed -i "s/${old_text}/${new_text}/g" {} +
    fi
}

# Step 1: Rename Java files
echo -e "${GREEN}Step 1: Renaming Java files...${NC}"
rename_files "SenderAdapter" "InboundAdapter" "adapters"
rename_files "ReceiverAdapter" "OutboundAdapter" "adapters"
rename_files "SenderAdapter" "InboundAdapter" "backend"
rename_files "ReceiverAdapter" "OutboundAdapter" "backend"

# Step 2: Update Java imports and class names
echo -e "${GREEN}Step 2: Updating Java code...${NC}"
update_contents "SenderAdapter" "InboundAdapter" "*.java"
update_contents "ReceiverAdapter" "OutboundAdapter" "*.java"
update_contents "SenderAdapterPort" "InboundAdapterPort" "*.java"
update_contents "ReceiverAdapterPort" "OutboundAdapterPort" "*.java"
update_contents "AbstractSenderAdapter" "AbstractInboundAdapter" "*.java"
update_contents "AbstractReceiverAdapter" "AbstractOutboundAdapter" "*.java"

# Step 3: Update enum values
echo -e "${GREEN}Step 3: Updating enum values...${NC}"
update_contents "SENDER" "INBOUND" "*.java"
update_contents "RECEIVER" "OUTBOUND" "*.java"

# Step 4: Update string literals
echo -e "${GREEN}Step 4: Updating string literals...${NC}"
update_contents '"sender"' '"inbound"' "*.java"
update_contents '"receiver"' '"outbound"' "*.java"
update_contents "'sender'" "'inbound'" "*.java"
update_contents "'receiver'" "'outbound'" "*.java"

# Step 5: Update comments
echo -e "${GREEN}Step 5: Updating comments...${NC}"
update_contents "Sender adapter" "Inbound adapter" "*.java"
update_contents "Receiver adapter" "Outbound adapter" "*.java"
update_contents "sender adapter" "inbound adapter" "*.java"
update_contents "receiver adapter" "outbound adapter" "*.java"
update_contents "Sender =" "Inbound =" "*.java"
update_contents "Receiver =" "Outbound =" "*.java"

# Step 6: Update TypeScript/JavaScript files
echo -e "${GREEN}Step 6: Updating TypeScript/JavaScript files...${NC}"
update_contents "SENDER" "INBOUND" "*.ts"
update_contents "RECEIVER" "OUTBOUND" "*.ts"
update_contents "SENDER" "INBOUND" "*.tsx"
update_contents "RECEIVER" "OUTBOUND" "*.tsx"
update_contents "sender" "inbound" "*.ts"
update_contents "receiver" "outbound" "*.ts"
update_contents "sender" "inbound" "*.tsx"
update_contents "receiver" "outbound" "*.tsx"
update_contents "Sender" "Inbound" "*.ts"
update_contents "Receiver" "Outbound" "*.ts"
update_contents "Sender" "Inbound" "*.tsx"
update_contents "Receiver" "Outbound" "*.tsx"

# Step 7: Update source/target references
echo -e "${GREEN}Step 7: Updating source/target references...${NC}"
update_contents "source_adapter" "inbound_adapter" "*.java"
update_contents "target_adapter" "outbound_adapter" "*.java"
update_contents "sourceAdapter" "inboundAdapter" "*.java"
update_contents "targetAdapter" "outboundAdapter" "*.java"
update_contents "sourceAdapter" "inboundAdapter" "*.ts"
update_contents "targetAdapter" "outboundAdapter" "*.ts"
update_contents "sourceAdapter" "inboundAdapter" "*.tsx"
update_contents "targetAdapter" "outboundAdapter" "*.tsx"

# Step 8: Update SQL files
echo -e "${GREEN}Step 8: Looking for SQL files to update...${NC}"
update_contents "SENDER" "INBOUND" "*.sql"
update_contents "RECEIVER" "OUTBOUND" "*.sql"
update_contents "source_adapter_id" "inbound_adapter_id" "*.sql"
update_contents "target_adapter_id" "outbound_adapter_id" "*.sql"

# Step 9: Generate summary report
echo -e "${GREEN}Generating refactoring report...${NC}"
cat > REFACTORING_REPORT.md << EOF
# Adapter Naming Refactoring Report

Generated on: $(date)

## Files Renamed

### Java Files
$(find adapters backend -name "*InboundAdapter*.java" -o -name "*OutboundAdapter*.java" | wc -l) files renamed

### Configuration Files
$(find adapters backend -name "*InboundAdapterConfig*.java" -o -name "*OutboundAdapterConfig*.java" | wc -l) configuration files renamed

## Code Updates

### Java Files Updated
$(grep -r "InboundAdapter\|OutboundAdapter\|INBOUND\|OUTBOUND" --include="*.java" . | cut -d: -f1 | sort -u | wc -l) files updated

### TypeScript Files Updated
$(grep -r "inbound\|outbound\|INBOUND\|OUTBOUND" --include="*.ts" --include="*.tsx" frontend/src 2>/dev/null | cut -d: -f1 | sort -u | wc -l) files updated

## Next Steps

1. Review the changes carefully
2. Run the test suite
3. Update the database using the migration scripts
4. Test the application thoroughly
5. Update any external documentation

## Backup Location
$BACKUP_DIR

EOF

echo -e "${GREEN}Refactoring complete! Check REFACTORING_REPORT.md for details.${NC}"
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Review the changes: git diff"
echo "2. Run tests: mvn test"
echo "3. Run database migration scripts"
echo "4. Test the application"
echo -e "${YELLOW}If you need to rollback:${NC}"
echo "cp -r $BACKUP_DIR/* ."