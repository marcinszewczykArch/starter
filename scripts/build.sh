#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🔨 Building Starter project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

cd "$PROJECT_ROOT"

# Build backend
echo ""
echo "☕ Building backend..."
./gradlew clean build -x test
echo -e "${GREEN}✅ Backend built successfully${NC}"

# Build frontend
echo ""
echo "⚛️  Building frontend..."
cd "$PROJECT_ROOT/frontend"

if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm ci
fi

npm run build
echo -e "${GREEN}✅ Frontend built successfully${NC}"

echo ""
echo -e "${GREEN}🎉 Build complete!${NC}"
echo ""
echo "Artifacts:"
echo "   Backend JAR:  backend/main/build/libs/app.jar"
echo "   Frontend:     frontend/dist/"

