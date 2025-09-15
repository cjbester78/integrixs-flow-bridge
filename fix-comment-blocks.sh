#!/bin/bash

# Fix incomplete comment blocks in all Java files

echo "Fixing incomplete comment blocks..."

# Find all files with the problematic pattern
find adapters/src/main/java -name "*.java" -exec grep -l "// DUPLICATE:" {} \; | while read file; do
    echo "Checking $file..."
    
    # Fix the specific pattern where comment block is incomplete
    sed -i.bak -E '
    /\/\/ DUPLICATE:.*{$/{
        N
        s/\/\/ DUPLICATE: (.*){\n\/\//\/\/ DUPLICATE: \1{\n    \/\//
        :loop
        N
        /\/\/ *}$/!b loop
        s/(\/\/ *})(\n\/\/ *public)/\1\n    \n    public/
    }' "$file"
    
    # Also fix the simpler case where return statement follows incomplete comment
    sed -i -E '
    /^\/\/ *}$/{
        N
        /\n *return /{
            s/\/\/ *}\n\/\//\/\/ }\n    \n    public/
        }
    }' "$file"
done

echo "Comment blocks fixed!"