#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 Starting Starter development environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command_exists docker; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

if ! command_exists java; then
    echo -e "${RED}❌ Java is not installed. Please install Java 21 first.${NC}"
    exit 1
fi

if ! command_exists node; then
    echo -e "${RED}❌ Node.js is not installed. Please install Node.js first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ All prerequisites met${NC}"

# Start PostgreSQL
echo ""
echo "🐘 Starting PostgreSQL..."
docker compose -f "$PROJECT_ROOT/infra/docker-compose.dev.yml" up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
until docker compose -f "$PROJECT_ROOT/infra/docker-compose.dev.yml" exec -T postgres pg_isready -U postgres -d starter >/dev/null 2>&1; do
    sleep 1
done
echo -e "${GREEN}✅ PostgreSQL is ready${NC}"

# Install frontend dependencies if needed
if [ ! -d "$PROJECT_ROOT/frontend/node_modules" ]; then
    echo ""
    echo "📦 Installing frontend dependencies..."
    cd "$PROJECT_ROOT/frontend"
    npm ci
fi

# Start backend in background
echo ""
echo "☕ Starting backend..."
cd "$PROJECT_ROOT"
SPRING_PROFILES_ACTIVE=local ./gradlew :backend:main:bootRun &
BACKEND_PID=$!

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
until curl -s http://localhost:8080/actuator/health >/dev/null 2>&1; do
    sleep 2
done
echo -e "${GREEN}✅ Backend is ready at http://localhost:8080${NC}"

# Start frontend
echo ""
echo "⚛️  Starting frontend..."
cd "$PROJECT_ROOT/frontend"
npm run dev &
FRONTEND_PID=$!

echo ""
echo -e "${GREEN}🎉 Development environment is ready!${NC}"
echo ""
echo "📍 Endpoints:"
echo "   Frontend:    http://localhost:5173"
echo "   Backend:     http://localhost:8080"
echo "   Swagger UI:  http://localhost:8080/swagger-ui.html"
echo "   Health:      http://localhost:8080/actuator/health"
echo ""
echo "Press Ctrl+C to stop all services..."

# Cleanup function
cleanup() {
    echo ""
    echo "🛑 Stopping services..."
    
    if [ -n "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
    
    if [ -n "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    
    docker compose -f "$PROJECT_ROOT/infra/docker-compose.dev.yml" down
    
    echo -e "${GREEN}✅ All services stopped${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# Wait for processes
wait

