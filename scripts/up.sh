#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting full Docker Compose stack..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

cd "$PROJECT_ROOT"

# Build and start all services
echo ""
echo "🐳 Building and starting containers..."
docker compose -f infra/docker-compose.yml up --build -d

echo ""
echo "⏳ Waiting for services to be healthy..."

# Wait for backend to be healthy
until curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; do
    echo "   Waiting for backend..."
    sleep 5
done

echo ""
echo -e "${GREEN}🎉 All services are running!${NC}"
echo ""
echo "📍 Endpoints:"
echo "   Frontend:    http://localhost:5173"
echo "   Backend:     http://localhost:8080"
echo "   Swagger UI:  http://localhost:8080/swagger-ui.html"
echo "   Health:      http://localhost:8080/actuator/health"
echo ""
echo "To stop: docker compose -f infra/docker-compose.yml down"

