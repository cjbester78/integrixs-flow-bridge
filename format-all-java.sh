#!/bin/bash

# Script to format all Java files in the project
# This applies consistent formatting rules across the codebase

echo "Starting Java code formatting..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Counter for files processed
TOTAL_FILES=0
FORMATTED_FILES=0
ERROR_FILES=0

# Create a temporary file to store errors
ERROR_LOG="/tmp/format-errors-$$.log"
> "$ERROR_LOG"

# Function to format a single Java file
format_java_file() {
    local file="$1"
    echo -n "Formatting: $file... "
    
    # Create backup
    cp "$file" "$file.bak"
    
    # Apply formatting rules
    # 1. Fix indentation (convert tabs to 4 spaces)
    sed -i '' 's/	/    /g' "$file"
    
    # 2. Fix trailing whitespace
    sed -i '' 's/[[:space:]]*$//' "$file"
    
    # 3. Ensure file ends with newline
    if [ -n "$(tail -c 1 "$file")" ]; then
        echo >> "$file"
    fi
    
    # 4. Fix spacing around operators (basic cases)
    # Add space after comma
    sed -i '' 's/,\([^ ]\)/, \1/g' "$file"
    
    # Add space around = (but not ==, !=, <=, >=)
    sed -i '' 's/\([^=!<>]\)=\([^=]\)/\1 = \2/g' "$file"
    
    # Add space around + (but not ++)
    sed -i '' 's/\([^+]\)+\([^+]\)/\1 + \2/g' "$file"
    
    # Add space around - (but not --)
    sed -i '' 's/\([^-]\)-\([^->]\)/\1 - \2/g' "$file"
    
    # 5. Fix opening brace placement (should be on same line)
    sed -i '' ':a;N;$!ba;s/\n[[:space:]]*{/ {/g' "$file"
    
    # Check if file changed
    if ! diff -q "$file" "$file.bak" > /dev/null 2>&1; then
        echo -e "${GREEN}Formatted${NC}"
        ((FORMATTED_FILES++))
        rm "$file.bak"
    else
        echo "No changes needed"
        rm "$file.bak"
    fi
    
    ((TOTAL_FILES++))
}

# Find all Java files (excluding target directories and generated files)
echo "Finding Java files to format..."

while IFS= read -r file; do
    format_java_file "$file"
done < <(find . -name "*.java" -type f | grep -v "/target/" | grep -v "/.git/" | grep -v "/generated/")

echo ""
echo "========================================="
echo -e "${GREEN}Formatting complete!${NC}"
echo "Total files processed: $TOTAL_FILES"
echo "Files formatted: $FORMATTED_FILES"

if [ -s "$ERROR_LOG" ]; then
    echo -e "${RED}Errors encountered: $ERROR_FILES${NC}"
    echo "Error details in: $ERROR_LOG"
else
    rm "$ERROR_LOG"
fi

echo ""
echo "Next steps:"
echo "1. Review changes with: git diff"
echo "2. Compile to verify: mvn compile"
echo "3. Commit changes: git add -A && git commit -m 'style: Apply consistent code formatting'"