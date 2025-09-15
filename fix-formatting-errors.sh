#!/bin/bash

# Fix formatting errors script for Integrix Flow Bridge

echo "Fixing formatting errors in Java files..."

# Fix double spaces in method signatures and class declarations
find . -name "*.java" -type f | while read -r file; do
    # Skip if file doesn't exist or is in .git
    if [[ "$file" == *".git"* ]]; then
        continue
    fi
    
    # Fix double spaces between type and opening brace
    sed -i '' 's/  {/ {/g' "$file"
    
    # Fix double spaces in implements/extends
    sed -i '' 's/extends  /extends /g' "$file"
    sed -i '' 's/implements  /implements /g' "$file"
    
    # Fix method signatures with double spaces
    sed -i '' 's/public  /public /g' "$file"
    sed -i '' 's/private  /private /g' "$file"
    sed -i '' 's/protected  /protected /g' "$file"
    sed -i '' 's/static  /static /g' "$file"
    sed -i '' 's/final  /final /g' "$file"
    sed -i '' 's/abstract  /abstract /g' "$file"
    sed -i '' 's/synchronized  /synchronized /g' "$file"
    
    # Fix multiple spaces in parameter lists
    sed -i '' 's/,  /, /g' "$file"
    sed -i '' 's/(  /( /g' "$file"
    sed -i '' 's/  )/ )/g' "$file"
    
    # Fix constructor double spaces
    sed -i '' 's/()  {/() {/g' "$file"
    
    # Remove any remaining double spaces that shouldn't be there
    sed -i '' 's/\([^[:space:]]\)  \([^[:space:]]\)/\1 \2/g' "$file"
done

echo "Formatting errors fixed."