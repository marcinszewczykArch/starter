#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🧪 Running all tests..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

cd "$PROJECT_ROOT"

# Run backend tests
echo ""
echo "☕ Running backend tests..."
./gradlew test
echo -e "${GREEN}✅ Backend tests passed${NC}"

# Run frontend tests
echo ""
echo "⚛️  Running frontend tests..."
cd "$PROJECT_ROOT/frontend"

if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm ci
fi

npm run test
echo -e "${GREEN}✅ Frontend tests passed${NC}"

echo ""
echo -e "${GREEN}🎉 All tests passed!${NC}"

