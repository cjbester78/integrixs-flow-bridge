#!/bin/bash

# Integration Test Runner Script
# This script runs all integration tests with proper setup and reporting

echo "========================================="
echo "Running Integration Tests for Integrix Flow Bridge"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}Docker is not running. Please start Docker first.${NC}"
        exit 1
    fi
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Maven is not installed. Please install Maven first.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}Prerequisites check passed.${NC}"
}

# Function to clean up containers
cleanup_containers() {
    echo -e "${YELLOW}Cleaning up test containers...${NC}"
    docker ps -a | grep "testcontainers" | awk '{print $1}' | xargs -r docker rm -f
    docker ps -a | grep "ryuk" | awk '{print $1}' | xargs -r docker rm -f
}

# Function to run tests
run_tests() {
    echo -e "${YELLOW}Running integration tests...${NC}"
    
    # Set test environment variables
    export TESTCONTAINERS_RYUK_DISABLED=false
    export TESTCONTAINERS_REUSE_ENABLE=true
    
    # Run tests with Maven
    cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend
    
    mvn clean verify -P integration-tests \
        -DskipUnitTests=true \
        -Dtest.containers.reuse.enable=true \
        -Dspring.profiles.active=test \
        -pl integration-tests \
        -am
    
    TEST_RESULT=$?
    
    return $TEST_RESULT
}

# Function to generate test report
generate_report() {
    echo -e "${YELLOW}Generating test report...${NC}"
    
    # Check if test results exist
    RESULTS_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/integration-tests/target/failsafe-reports"
    
    if [ -d "$RESULTS_DIR" ]; then
        # Count test results
        TOTAL_TESTS=$(find $RESULTS_DIR -name "*.txt" | wc -l)
        FAILED_TESTS=$(grep -l "FAILURE" $RESULTS_DIR/*.txt 2>/dev/null | wc -l)
        PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS))
        
        echo "========================================="
        echo "Integration Test Results:"
        echo "Total Tests: $TOTAL_TESTS"
        echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
        echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
        echo "========================================="
        
        # Show failed test details if any
        if [ $FAILED_TESTS -gt 0 ]; then
            echo -e "${RED}Failed Tests:${NC}"
            grep -l "FAILURE" $RESULTS_DIR/*.txt | while read file; do
                TEST_NAME=$(basename $file .txt)
                echo "  - $TEST_NAME"
            done
        fi
    else
        echo -e "${RED}No test results found.${NC}"
    fi
}

# Main execution
main() {
    echo "Starting at $(date)"
    
    # Check prerequisites
    check_prerequisites
    
    # Clean up old containers
    cleanup_containers
    
    # Run tests
    run_tests
    TEST_EXIT_CODE=$?
    
    # Generate report
    generate_report
    
    # Final cleanup
    cleanup_containers
    
    echo "Completed at $(date)"
    
    if [ $TEST_EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}All integration tests passed!${NC}"
    else
        echo -e "${RED}Some integration tests failed!${NC}"
        exit $TEST_EXIT_CODE
    fi
}

# Run main function
main