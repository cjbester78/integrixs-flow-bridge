#!/bin/bash

echo "🔍 Comprehensive JPA search - checking every file..."

# Search for JPA patterns in all source files
echo "Searching for: JPA, jpa, Jpa, hibernate, Hibernate, EntityManager, PersistenceContext, @EnableJpaRepositories"

# Get all source files (exclude target, node_modules, .git)
FILES=$(find /Users/cjbester/git/Integrixs-Flow-Bridge -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -o -name "*.md" \) | grep -v target | grep -v node_modules | grep -v .git | sort)

FOUND_COUNT=0
TOTAL_COUNT=0

for file in $FILES; do
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    
    # Search for JPA patterns in the file
    MATCHES=$(grep -n -E "(JPA|jpa|Jpa|hibernate|Hibernate|EntityManager|PersistenceContext|Repository.*extends.*JpaRepository|spring-boot-starter-data-jpa|@EnableJpaRepositories)" "$file" 2>/dev/null)
    
    if [ -n "$MATCHES" ]; then
        FOUND_COUNT=$((FOUND_COUNT + 1))
        echo "🚨 FOUND JPA REFERENCES in: $file"
        echo "$MATCHES"
        echo "---"
    fi
done

echo ""
echo "📊 Search completed!"
echo "   Total files searched: $TOTAL_COUNT"
echo "   Files with JPA references: $FOUND_COUNT"

if [ $FOUND_COUNT -eq 0 ]; then
    echo "✅ No JPA references found! Codebase is clean."
else
    echo "⚠️  JPA references still found in $FOUND_COUNT files."
fi