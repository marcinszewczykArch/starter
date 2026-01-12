#!/bin/bash
set -e
cd "$(dirname "$0")/.."

echo "🧪 Running Tests"

echo "🐳 Starting test database..."
docker compose -f infra/docker-compose.test.yml down -v 2>/dev/null || true
docker compose -f infra/docker-compose.test.yml up -d
until docker compose -f infra/docker-compose.test.yml exec -T postgres pg_isready -U postgres -d starter >/dev/null 2>&1; do sleep 1; done

echo "☕ Backend tests..."
./gradlew test --no-daemon -q

echo "⚛️  Frontend tests..."
cd frontend
npm test --silent

echo "✅ All tests passed"
