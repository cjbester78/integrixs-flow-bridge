#!/bin/bash

# Script to apply consistent formatting to all Java files
# Based on the project's formatting rules

echo "Starting Java code formatting for all files..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Counters
TOTAL_FILES=0
FORMATTED_FILES=0
SKIPPED_FILES=0
ERROR_FILES=0

# Create a log file
LOG_FILE="formatting-log-$(date +%Y%m%d-%H%M%S).txt"
echo "Formatting started at $(date)" > "$LOG_FILE"

# Function to format a single Java file
format_java_file() {
    local file="$1"
    local relative_path="${file#./}"
    
    # Skip target directories and generated files
    if [[ "$file" == *"/target/"* ]] || [[ "$file" == *"/generated/"* ]] || [[ "$file" == *"/.git/"* ]]; then
        ((SKIPPED_FILES++))
        return
    fi
    
    echo -n "Formatting: $relative_path... "
    echo "Processing: $relative_path" >> "$LOG_FILE"
    
    # Create backup
    cp "$file" "$file.bak" 2>/dev/null || {
        echo -e "${RED}Failed to create backup${NC}"
        ((ERROR_FILES++))
        return
    }
    
    # Apply formatting rules
    
    # 1. Convert tabs to 4 spaces
    sed -i '' 's/	/    /g' "$file" 2>/dev/null
    
    # 2. Remove trailing whitespace
    sed -i '' 's/[[:space:]]*$//' "$file" 2>/dev/null
    
    # 3. Ensure file ends with newline
    if [ -n "$(tail -c 1 "$file" 2>/dev/null)" ]; then
        echo >> "$file"
    fi
    
    # 4. Fix spacing around common operators (conservative approach)
    # Add space after comma (but not in generics or between empty parens)
    sed -i '' 's/,\([^ >)\]]\)/, \1/g' "$file" 2>/dev/null
    
    # Add space around = (but not ==, !=, <=, >=, +=, -=, etc.)
    sed -i '' 's/\([^ =!<>+*\/-]\)=\([^ =]\)/\1 = \2/g' "$file" 2>/dev/null
    
    # Add space around + (but not ++, +=)
    sed -i '' 's/\([^ +]\)+\([^ +=]\)/\1 + \2/g' "$file" 2>/dev/null
    
    # Add space around - (but not --, -=, ->)
    sed -i '' 's/\([^ -]\)-\([^ -=>]\)/\1 - \2/g' "$file" 2>/dev/null
    
    # 5. Fix spacing around braces
    # Add space before opening brace (but not array initializers)
    sed -i '' 's/\([^{]\){/\1 {/g' "$file" 2>/dev/null
    
    # 6. Fix if/for/while spacing
    sed -i '' 's/if(/if (/g' "$file" 2>/dev/null
    sed -i '' 's/for(/for (/g' "$file" 2>/dev/null
    sed -i '' 's/while(/while (/g' "$file" 2>/dev/null
    sed -i '' 's/switch(/switch (/g' "$file" 2>/dev/null
    sed -i '' 's/catch(/catch (/g' "$file" 2>/dev/null
    
    # 7. Ensure spaces after keywords
    sed -i '' 's/\(public\|private\|protected\|static\|final\|abstract\|synchronized\|volatile\)\([^ ]\)/\1 \2/g' "$file" 2>/dev/null
    
    # 8. Fix method declaration spacing
    sed -i '' 's/\([a-zA-Z0-9_]\)(/\1 (/g' "$file" 2>/dev/null
    sed -i '' 's/\([a-zA-Z0-9_>]\) (/\1(/g' "$file" 2>/dev/null  # Remove space before method params
    
    # Check if file changed
    if ! diff -q "$file" "$file.bak" > /dev/null 2>&1; then
        echo -e "${GREEN}Formatted${NC}"
        echo "  - File was formatted" >> "$LOG_FILE"
        ((FORMATTED_FILES++))
        rm "$file.bak"
    else
        echo "No changes needed"
        echo "  - No changes needed" >> "$LOG_FILE"
        rm "$file.bak"
    fi
    
    ((TOTAL_FILES++))
}

# Process files by module for better organization
echo -e "\n${BLUE}Processing modules...${NC}\n"

# Define modules in order
MODULES=(
    "shared-lib"
    "adapters"
    "backend"
    "data-access"
    "engine"
    "monitoring"
    "webclient"
    "webserver"
    "soap-bindings"
    "integration-tests"
)

# Process each module
for module in "${MODULES[@]}"; do
    if [ -d "$module" ]; then
        echo -e "\n${YELLOW}Module: $module${NC}"
        echo -e "\nModule: $module" >> "$LOG_FILE"
        
        # Find and process Java files in this module
        while IFS= read -r file; do
            format_java_file "$file"
        done < <(find "$module" -name "*.java" -type f 2>/dev/null)
    fi
done

# Summary
echo -e "\n${BLUE}=========================================${NC}"
echo -e "${GREEN}Formatting complete!${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""
echo "Summary:" | tee -a "$LOG_FILE"
echo "  Total files processed: $TOTAL_FILES" | tee -a "$LOG_FILE"
echo "  Files formatted: $FORMATTED_FILES" | tee -a "$LOG_FILE"
echo "  Files skipped: $SKIPPED_FILES" | tee -a "$LOG_FILE"
if [ $ERROR_FILES -gt 0 ]; then
    echo -e "  ${RED}Files with errors: $ERROR_FILES${NC}" | tee -a "$LOG_FILE"
fi
echo ""
echo "Log file created: $LOG_FILE"
echo ""
echo "Next steps:"
echo "1. Review changes with: git diff"
echo "2. Run compilation check: mvn clean compile"
echo "3. If satisfied, commit changes: git add -A && git commit -m 'style: Apply consistent Java formatting to all files'"
echo ""
echo "Formatting completed at $(date)" >> "$LOG_FILE"