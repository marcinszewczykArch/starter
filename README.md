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
- **Health Check**: http://localhost:8080/actuator/health

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
│       │       ├── feature/    # Feature flags
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
│   └── test.sh                 # Run all tests
├── config/                     # Shared config
│   └── checkstyle/             # Checkstyle rules
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

# Lint check
./gradlew lint

# Format code
./gradlew format

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

## 🔌 API Endpoints

### Example API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/example` | List all examples |
| GET | `/api/v1/example/{id}` | Get example by ID |
| POST | `/api/v1/example` | Create new example |
| PUT | `/api/v1/example/{id}` | Update example |
| DELETE | `/api/v1/example/{id}` | Delete example |
| GET | `/api/v1/example/feature-status` | Check feature flag |

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Metrics |
| `/actuator/prometheus` | Prometheus metrics |

## 🧪 Testing

### Backend Tests

Backend uses Testcontainers for integration tests. No local database required.

```bash
./gradlew test
```

Tests include:
- Application context loading
- Flyway migrations
- Repository operations
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

## 🏷 Feature Flags

Feature flags are configured in `application.yml`:

```yaml
app:
  feature-flags:
    example-feature: true
```

Usage in code:

```java
@Autowired
private FeatureFlagService featureFlagService;

if (featureFlagService.isEnabled(FeatureFlag.EXAMPLE_FEATURE)) {
    // Feature-specific code
}
```

## 📝 Tech Stack

### Backend
- Java 21
- Spring Boot 3.2
- Spring JDBC (JdbcClient)
- PostgreSQL 16
- Flyway
- SpringDoc OpenAPI (Swagger)
- Testcontainers
- Spotless + Checkstyle

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

