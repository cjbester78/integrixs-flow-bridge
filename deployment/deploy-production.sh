#!/bin/bash

# Production deployment script for Integrix Flow Bridge
# This script handles the complete deployment process

set -e  # Exit on error

echo "========================================="
echo "Integrix Flow Bridge Production Deployment"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DEPLOYMENT_ENV=${1:-production}
VERSION=${2:-latest}
NAMESPACE=${3:-integrix}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    local errors=0
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}❌ Docker is not installed${NC}"
        ((errors++))
    else
        echo -e "${GREEN}✓ Docker found${NC}"
    fi
    
    # Check kubectl (for K8s deployment)
    if ! command -v kubectl &> /dev/null; then
        echo -e "${YELLOW}⚠ kubectl not found (required for Kubernetes deployment)${NC}"
    else
        echo -e "${GREEN}✓ kubectl found${NC}"
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Maven is not installed${NC}"
        ((errors++))
    else
        echo -e "${GREEN}✓ Maven found${NC}"
    fi
    
    if [ $errors -gt 0 ]; then
        echo -e "${RED}Please install missing prerequisites before continuing.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}All prerequisites satisfied!${NC}"
}

# Function to run tests
run_tests() {
    echo -e "${YELLOW}Running tests...${NC}"
    
    # Run unit tests
    echo -e "${BLUE}Running unit tests...${NC}"
    mvn test || {
        echo -e "${RED}Unit tests failed!${NC}"
        exit 1
    }
    
    echo -e "${GREEN}✓ Unit tests passed${NC}"
    
    # Run integration tests (optional)
    if [ "$SKIP_INTEGRATION_TESTS" != "true" ]; then
        echo -e "${BLUE}Running integration tests...${NC}"
        mvn verify -P integration-tests || {
            echo -e "${RED}Integration tests failed!${NC}"
            exit 1
        }
        echo -e "${GREEN}✓ Integration tests passed${NC}"
    fi
}

# Function to build application
build_application() {
    echo -e "${YELLOW}Building application...${NC}"
    
    # Clean and build
    mvn clean package -DskipTests -P production || {
        echo -e "${RED}Build failed!${NC}"
        exit 1
    }
    
    echo -e "${GREEN}✓ Application built successfully${NC}"
}

# Function to build Docker image
build_docker_image() {
    echo -e "${YELLOW}Building Docker image...${NC}"
    
    cd deployment
    
    # Build image
    docker build -t integrix/flow-bridge:${VERSION} -f Dockerfile .. || {
        echo -e "${RED}Docker build failed!${NC}"
        exit 1
    }
    
    # Tag as latest
    docker tag integrix/flow-bridge:${VERSION} integrix/flow-bridge:latest
    
    cd ..
    
    echo -e "${GREEN}✓ Docker image built: integrix/flow-bridge:${VERSION}${NC}"
}

# Function to deploy with Docker Compose
deploy_docker_compose() {
    echo -e "${YELLOW}Deploying with Docker Compose...${NC}"
    
    cd deployment
    
    # Create .env file if not exists
    if [ ! -f .env ]; then
        echo -e "${YELLOW}Creating .env file...${NC}"
        cat > .env <<EOF
SPRING_PROFILE=${DEPLOYMENT_ENV}
DB_PASSWORD=$(openssl rand -base64 32)
REDIS_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
GRAFANA_PASSWORD=admin
EOF
        echo -e "${GREEN}✓ Created .env file with secure passwords${NC}"
    fi
    
    # Pull latest images
    docker-compose pull
    
    # Deploy
    docker-compose up -d || {
        echo -e "${RED}Docker Compose deployment failed!${NC}"
        exit 1
    }
    
    cd ..
    
    echo -e "${GREEN}✓ Application deployed with Docker Compose${NC}"
}

# Function to deploy to Kubernetes
deploy_kubernetes() {
    echo -e "${YELLOW}Deploying to Kubernetes...${NC}"
    
    cd deployment/kubernetes
    
    # Create namespace
    kubectl apply -f namespace.yaml
    
    # Create secrets if not exist
    if ! kubectl get secret integrix-secrets -n ${NAMESPACE} &> /dev/null; then
        echo -e "${YELLOW}Creating Kubernetes secrets...${NC}"
        kubectl create secret generic integrix-secrets \
            --from-literal=jwt-secret=$(openssl rand -base64 64) \
            --from-literal=db-password=$(openssl rand -base64 32) \
            --from-literal=redis-password=$(openssl rand -base64 32) \
            -n ${NAMESPACE}
    fi
    
    # Apply configurations
    kubectl apply -f configmap.yaml
    kubectl apply -f pv.yaml
    kubectl apply -f pvc.yaml
    kubectl apply -f service.yaml
    kubectl apply -f deployment.yaml
    kubectl apply -f ingress.yaml
    
    cd ../..
    
    echo -e "${GREEN}✓ Application deployed to Kubernetes${NC}"
}

# Function to verify deployment
verify_deployment() {
    echo -e "${YELLOW}Verifying deployment...${NC}"
    
    if [ "$DEPLOYMENT_TYPE" == "docker-compose" ]; then
        # Check Docker containers
        echo -e "${BLUE}Checking containers...${NC}"
        docker-compose ps
        
        # Wait for application to be ready
        echo -e "${BLUE}Waiting for application to be ready...${NC}"
        sleep 30
        
        # Health check
        curl -f http://localhost:8080/actuator/health || {
            echo -e "${RED}Health check failed!${NC}"
            exit 1
        }
    else
        # Check Kubernetes pods
        echo -e "${BLUE}Checking pods...${NC}"
        kubectl get pods -n ${NAMESPACE}
        
        # Wait for pods to be ready
        kubectl wait --for=condition=ready pod -l app=integrix-app -n ${NAMESPACE} --timeout=300s
        
        # Health check via port-forward
        kubectl port-forward -n ${NAMESPACE} deployment/integrix-app 8080:8080 &
        PF_PID=$!
        sleep 5
        
        curl -f http://localhost:8080/actuator/health || {
            echo -e "${RED}Health check failed!${NC}"
            kill $PF_PID
            exit 1
        }
        
        kill $PF_PID
    fi
    
    echo -e "${GREEN}✓ Deployment verified successfully${NC}"
}

# Function to show post-deployment info
show_info() {
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN}Deployment completed successfully!${NC}"
    echo -e "${GREEN}=========================================${NC}"
    echo ""
    echo -e "${BLUE}Access Information:${NC}"
    
    if [ "$DEPLOYMENT_TYPE" == "docker-compose" ]; then
        echo "- Application: http://localhost:8080"
        echo "- Prometheus: http://localhost:9090"
        echo "- Grafana: http://localhost:3001 (admin/admin)"
        echo ""
        echo -e "${YELLOW}View logs:${NC} docker-compose logs -f integrix-app"
    else
        echo "- Application: Via configured ingress"
        echo ""
        echo -e "${YELLOW}View logs:${NC} kubectl logs -f -n ${NAMESPACE} -l app=integrix-app"
    fi
    
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo "1. Update DNS records if needed"
    echo "2. Configure SSL certificates"
    echo "3. Set up monitoring alerts"
    echo "4. Review security settings"
}

# Main deployment flow
main() {
    echo "Deployment configuration:"
    echo "- Environment: ${DEPLOYMENT_ENV}"
    echo "- Version: ${VERSION}"
    echo ""
    
    # Check prerequisites
    check_prerequisites
    
    # Ask for deployment type
    echo -e "${BLUE}Select deployment type:${NC}"
    echo "1) Docker Compose"
    echo "2) Kubernetes"
    read -p "Enter choice (1 or 2): " choice
    
    case $choice in
        1)
            DEPLOYMENT_TYPE="docker-compose"
            ;;
        2)
            DEPLOYMENT_TYPE="kubernetes"
            ;;
        *)
            echo -e "${RED}Invalid choice!${NC}"
            exit 1
            ;;
    esac
    
    # Run tests
    run_tests
    
    # Build application
    build_application
    
    # Build Docker image
    build_docker_image
    
    # Deploy based on type
    if [ "$DEPLOYMENT_TYPE" == "docker-compose" ]; then
        deploy_docker_compose
    else
        deploy_kubernetes
    fi
    
    # Verify deployment
    verify_deployment
    
    # Show information
    show_info
}

# Run main function
main