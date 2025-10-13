#!/bin/bash

# Vending Inventory System - Quick Start Script
# This script helps you quickly start the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}╔═══════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Vending Inventory Management System - Startup   ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════╝${NC}"
echo ""

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✓${NC} Java found: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java not found. Please install Java 21 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -1 | awk '{print $3}')
    echo -e "${GREEN}✓${NC} Maven found: $MVN_VERSION"
else
    echo -e "${RED}✗${NC} Maven not found. Please install Maven 3.8 or higher."
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
    echo -e "${GREEN}✓${NC} Docker found: $DOCKER_VERSION"
    DOCKER_AVAILABLE=true
else
    echo -e "${YELLOW}⚠${NC} Docker not found. You'll need to setup MySQL manually."
    DOCKER_AVAILABLE=false
fi

echo ""
echo -e "${YELLOW}Select startup option:${NC}"
echo "1) Full Stack (Docker Compose - Recommended)"
echo "2) Backend Only (Manual MySQL)"
echo "3) Database Only (MySQL Docker)"
echo "4) Clean and Rebuild"
echo "5) Exit"
echo ""
read -p "Enter option (1-5): " option

case $option in
    1)
        echo -e "${GREEN}Starting full stack with Docker Compose...${NC}"
        if [ "$DOCKER_AVAILABLE" = true ]; then
            docker-compose up -d
            echo ""
            echo -e "${GREEN}✓${NC} Services started successfully!"
            echo ""
            echo -e "${YELLOW}Access points:${NC}"
            echo "  Backend API:  http://localhost:8080"
            echo "  Swagger UI:   http://localhost:8080/swagger-ui.html"
            echo "  Health Check: http://localhost:8080/actuator/health"
            echo "  MySQL:        localhost:3306"
            echo ""
            echo -e "${YELLOW}Default credentials:${NC}"
            echo "  Username: admin"
            echo "  Password: admin123"
            echo ""
            echo "Run 'docker-compose logs -f' to view logs"
        else
            echo -e "${RED}Docker is not available. Please install Docker first.${NC}"
            exit 1
        fi
        ;;

    2)
        echo -e "${GREEN}Starting backend only...${NC}"
        echo -e "${YELLOW}Make sure MySQL is running on localhost:3306${NC}"
        echo ""
        cd backend
        echo "Building application..."
        mvn clean install -DskipTests
        echo ""
        echo -e "${GREEN}Starting Spring Boot application...${NC}"
        mvn spring-boot:run
        ;;

    3)
        echo -e "${GREEN}Starting MySQL only...${NC}"
        if [ "$DOCKER_AVAILABLE" = true ]; then
            docker-compose up -d mysql
            echo ""
            echo -e "${GREEN}✓${NC} MySQL started successfully!"
            echo ""
            echo -e "${YELLOW}Connection details:${NC}"
            echo "  Host:     localhost"
            echo "  Port:     3306"
            echo "  Database: vending_inventory"
            echo "  Username: root"
            echo "  Password: password"
            echo ""
            echo "Test connection: mysql -h localhost -u root -p"
        else
            echo -e "${RED}Docker is not available. Please install Docker first.${NC}"
            exit 1
        fi
        ;;

    4)
        echo -e "${YELLOW}Cleaning and rebuilding...${NC}"
        echo ""

        # Stop containers if running
        if [ "$DOCKER_AVAILABLE" = true ]; then
            echo "Stopping Docker containers..."
            docker-compose down -v
        fi

        # Clean Maven build
        cd backend
        echo "Cleaning Maven build..."
        mvn clean

        echo ""
        echo -e "${GREEN}✓${NC} Cleanup complete!"
        echo "You can now run option 1 or 2 to start fresh."
        ;;

    5)
        echo "Exiting..."
        exit 0
        ;;

    *)
        echo -e "${RED}Invalid option${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  For more information, see README.md           ${NC}"
echo -e "${GREEN}════════════════════════════════════════════════${NC}"
