#!/bin/bash

# Validation script for CLAUDE.md compliance

echo "======================================"
echo "CLAUDE.md Compliance Validation"
echo "======================================"

VIOLATIONS=0

# Function to check for violations
check_violation() {
    local pattern=$1
    local description=$2
    local file_pattern=$3
    local severity=$4
    
    echo -n "Checking for $description..."
    
    local count=$(find . -name "$file_pattern" -type f -not -path "./target/*" -not -path "./.git/*" -not -path "./node_modules/*" -exec grep -l "$pattern" {} \; | wc -l)
    
    if [ $count -gt 0 ]; then
        echo " FAILED ($count violations)"
        echo "  Severity: $severity"
        echo "  Files with violations:"
        find . -name "$file_pattern" -type f -not -path "./target/*" -not -path "./.git/*" -not -path "./node_modules/*" -exec grep -l "$pattern" {} \; | head -10 | sed 's/^/    /'
        if [ $count -gt 10 ]; then
            echo "    ... and $((count - 10)) more"
        fi
        VIOLATIONS=$((VIOLATIONS + count))
    else
        echo " PASSED"
    fi
    echo
}

# Check for TODO comments (excluding legitimate placeholders)
check_violation "//[ ]*TODO(?!:)" "TODO comments" "*.java" "critical"

# Check for UnsupportedOperationException
check_violation "throw new UnsupportedOperationException" "UnsupportedOperationException usage" "*.java" "high"

# Skip smtp.example.com as it's the standard RFC example domain for tests
check_violation "\"localhost:|\"127\\.0\\.0\\.1:|\"smtp\\.(?!example\\.com)|\"8080\"|\"5432\"" "hardcoded host/port values" "*.java" "critical"

# Check for placeholder variables in SQL (exclude legitimate JDBC placeholders)
# Only flag actual TODO placeholders, not JDBC ? placeholders
check_violation "TODO:|FIXME:|XXX:|PLACEHOLDER|\\[TODO\\]" "placeholder detection" "*.java" "high"

# Check for hardcoded credentials
check_violation "password[ ]*=[ ]*\"[^\"]+\"|secret[ ]*=[ ]*\"[^\"]+\"" "hardcoded credentials" "*.java" "critical"

# Check for System.out.println
check_violation "System\\.out\\.println" "System.out.println usage" "*.java" "medium"

# Check for printStackTrace
check_violation "\\.printStackTrace\\(\\)" "printStackTrace usage" "*.java" "medium"

echo "======================================"
echo "Total violations: $VIOLATIONS"
echo "======================================"

if [ $VIOLATIONS -eq 0 ]; then
    echo "✅ All checks passed!"
    exit 0
else
    echo "❌ Validation failed with $VIOLATIONS violations"
    exit 1
fi