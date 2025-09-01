#!/bin/bash

# Script to list and describe available scripts

echo "=== Integrix Flow Bridge Scripts ==="
echo ""
echo "Main deployment script:"
echo "  ./deploy.sh - Build and deploy the application"
echo ""

echo "Database scripts (scripts/database/):"
for script in scripts/database/*.sql; do
    if [[ -f "$script" ]]; then
        filename=$(basename "$script")
        echo "  - $filename"
    fi
done
echo ""

echo "Testing scripts (scripts/testing/):"
for script in scripts/testing/*; do
    if [[ -f "$script" ]]; then
        filename=$(basename "$script")
        echo "  - $filename"
    fi
done
echo ""

echo "Maintenance scripts (scripts/maintenance/):"
for script in scripts/maintenance/*.sh; do
    if [[ -f "$script" ]]; then
        filename=$(basename "$script")
        echo "  - $filename"
    fi
done
echo ""

echo "Migration scripts (scripts/migration/):"
echo "  Note: Most migration scripts are historical and no longer needed"
for script in scripts/migration/*; do
    if [[ -f "$script" ]]; then
        filename=$(basename "$script")
        echo "  - $filename"
    fi
done
echo ""

echo "For more details, see scripts/README.md"