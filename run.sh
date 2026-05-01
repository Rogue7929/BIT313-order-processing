#!/bin/bash

# Build and run scripts for Reactive Order Processing System

echo "================================"
echo "Reactive Order Processing System"
echo "BIT313 Phase 1"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo "Checking Java version..."
JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "Found: $JAVA_VERSION"
echo ""

# Build option
if [ "$1" = "build" ]; then
    echo "${YELLOW}Building project...${NC}"
    mvn clean package
    echo "${GREEN}Build complete!${NC}"
    exit 0
fi

# Run server
if [ "$1" = "server" ]; then
    echo "${YELLOW}Starting Reactive Order Processing Server...${NC}"
    echo "Listening on http://localhost:8080"
    echo "Press Ctrl+C to stop"
    echo ""
    mvn spring-boot:run
    exit 0
fi

# Run benchmark
if [ "$1" = "benchmark" ]; then
    echo "${YELLOW}Running load test with 5000 concurrent orders...${NC}"
    echo "This will take ~15-30 seconds..."
    echo ""
    mvn spring-boot:run -Dspring-boot.run.arguments="--runBenchmark"
    echo ""
    echo "${GREEN}Load test complete!${NC}"
    echo "Results saved to: benchmark-results.csv"
    exit 0
fi

# Default: show help
echo "${YELLOW}Usage:${NC}"
echo "  $0 build       - Build the project"
echo "  $0 server      - Run REST API server (http://localhost:8080)"
echo "  $0 benchmark   - Run load test with 5000 concurrent orders"
echo ""
echo "${YELLOW}Examples:${NC}"
echo "  # Build once"
echo "  $0 build"
echo ""
echo "  # Run server"
echo "  $0 server"
echo ""
echo "  # In another terminal, test an order:"
echo "  curl -X POST http://localhost:8080/api/orders \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"productId\":\"Laptop\",\"quantity\":1,\"price\":3500}'"
echo ""
echo "  # Run benchmark"
echo "  $0 benchmark"
echo ""
echo "${YELLOW}Make this script executable:${NC}"
echo "  chmod +x run.sh"
