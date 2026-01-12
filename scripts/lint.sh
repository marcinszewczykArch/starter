#!/bin/bash
set -e
cd "$(dirname "$0")/.."

echo "🔧 Lint & Format"

echo "☕ Backend..."
./gradlew lint --no-daemon -q

echo "⚛️  Frontend..."
cd frontend
npm run format --silent
npm run lint --silent

echo "✅ Done"
