# Starter

A production-ready monorepo template with Spring Boot backend and React frontend.

## 🚀 Quick Start

```bash
# Start development environment (PostgreSQL + Backend + Frontend)
./scripts/dev.sh
```

That's it! The application will be available at:
- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 📋 Requirements

- **Java 21** - [Download](https://adoptium.net/)
- **Node.js 20+** - [Download](https://nodejs.org/)
- **Docker** - [Download](https://www.docker.com/)

## 🏗 Project Structure

```
starter/
├── backend/                    # Java/Spring Boot backend
│   ├── core/                   # Shared utilities module
│   └── main/                   # Main application module
│       ├── src/main/java/
│       │   └── com/starter/
│       │       ├── config/     # Configuration classes
│       │       ├── controller/ # REST controllers
│       │       ├── domain/     # Domain entities
│       │       ├── dto/        # Data Transfer Objects
│       │       ├── repository/ # Database repositories
│       │       └── service/    # Business logic
│       └── src/main/resources/
│           └── db/migration/   # Flyway migrations
├── frontend/                   # React/Vite frontend
│   └── src/
│       ├── api/                # API client
│       ├── components/         # React components
│       └── test/               # Test files
├── infra/                      # Infrastructure files
│   ├── docker-compose.yml      # Full stack compose
│   ├── docker-compose.dev.yml  # Dev DB only
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   └── nginx.conf
├── scripts/                    # Utility scripts
│   ├── dev.sh                  # Start dev environment
│   ├── build.sh                # Build all
│   ├── up.sh                   # Start Docker stack
│   ├── test.sh                 # Run all tests
│   └── load-test.sh            # Performance load test
├── requests/                   # HTTP client requests (IntelliJ)
├── config/                     # Shared config
│   └── code-format.xml         # Eclipse formatter config
└── .github/
    └── workflows/
        └── ci.yml              # GitHub Actions CI
```

## 🛠 Available Commands

### Development

```bash
# Start full development environment
./scripts/dev.sh

# Run all tests
./scripts/test.sh

# Build everything
./scripts/build.sh
```

### Backend

```bash
# Run tests
./gradlew test

# Lint & format code
./gradlew lint

# Only check formatting (no changes)
./gradlew lintCheck

# Run application
./gradlew :backend:main:bootRun -Dspring.profiles.active=local
```

### Frontend

```bash
cd frontend

# Install dependencies
npm ci

# Start dev server
npm run dev

# Run tests
npm run test

# Lint
npm run lint

# Format
npm run format

# Build for production
npm run build
```

### Docker

```bash
# Start only PostgreSQL (for local development)
docker compose -f infra/docker-compose.dev.yml up -d

# Start full stack (PostgreSQL + Backend + Frontend)
./scripts/up.sh

# Stop all containers
docker compose -f infra/docker-compose.yml down
```

## 🧪 Testing

### Backend Tests

Backend uses Testcontainers for integration tests. No local database required.

```bash
./gradlew test
```

Tests include:
- Application context loading
- Flyway migrations
- REST endpoint integration tests

### Frontend Tests

Frontend uses Vitest with React Testing Library.

```bash
cd frontend
npm run test
```

## ⚙️ Configuration

### Backend Profiles

- `local` - Local development with local PostgreSQL
- `test` - Testing with Testcontainers

### Environment Variables

#### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | - | Active Spring profile |
| `SPRING_DATASOURCE_URL` | - | Database URL |
| `SPRING_DATASOURCE_USERNAME` | postgres | DB username |
| `SPRING_DATASOURCE_PASSWORD` | postgres | DB password |

#### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | (empty) | API base URL |

## 📝 Tech Stack

### Backend
- Java 21
- Spring Boot 3.2
- Spring JDBC (JdbcClient)
- PostgreSQL 16
- Flyway
- SpringDoc OpenAPI (Swagger)
- Testcontainers
- Spotless + Error Prone + NullAway

### Frontend
- React 18
- TypeScript
- Vite
- Vitest
- React Testing Library
- ESLint + Prettier

### Infrastructure
- Docker & Docker Compose
- Nginx
- GitHub Actions

## 📄 License

MIT
