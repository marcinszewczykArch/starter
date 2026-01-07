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
│   ├── docker-compose.dev.yml  # Dev database (port 5432)
│   ├── docker-compose.test.yml # Test database (port 5433)
│   ├── docker-compose.prod.yml # Production compose
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   ├── nginx.conf
│   ├── nginx.prod.conf
│   └── terraform/              # AWS infrastructure (EC2 + RDS)
├── scripts/                    # Utility scripts
│   ├── dev.sh                  # Start dev environment
│   ├── build.sh                # Build all
│   ├── up.sh                   # Start Docker stack
│   └── test.sh                 # Run all tests
├── requests/                   # HTTP client requests (IntelliJ)
├── config/                     # Shared config
│   └── code-format.xml         # Eclipse formatter config
└── .github/
    └── workflows/
        └── ci.yml              # GitHub Actions CI/CD (tests + deploy)
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
# Start dev database (port 5432)
docker compose -f infra/docker-compose.dev.yml up -d

# Start test database (port 5433)
docker compose -f infra/docker-compose.test.yml up -d

# Start full stack (PostgreSQL + Backend + Frontend)
./scripts/up.sh

# Stop all containers
docker compose -f infra/docker-compose.yml down
```

## 🧪 Testing

### Backend Tests

Integration tests use a separate PostgreSQL instance (port 5433) for isolation.

```bash
# Start test database and run tests
./scripts/test.sh

# Or manually:
docker compose -f infra/docker-compose.test.yml up -d
./gradlew test
```

Database is cleaned before each test for full isolation.

### Frontend Tests

Frontend uses Vitest with React Testing Library.

```bash
cd frontend
npm run test
```

## ⚙️ Configuration

### Backend Profiles

- `local` - Local development (PostgreSQL on port 5432)
- `test` - Testing (PostgreSQL on port 5433)
- `prod` - Production (RDS, Swagger protected with Basic Auth)

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
- Terraform (AWS EC2 + RDS)

## 🚀 Production Deployment

Deploy to AWS EC2 + RDS with automatic CI/CD from GitHub Actions.

### Prerequisites

- AWS Account
- Terraform installed (`brew install terraform`)
- AWS CLI configured (`aws configure`)

---

### Step 1: Create AWS Key Pair

1. Go to **AWS Console → EC2 → Key Pairs → Create key pair**
2. Name: `starter-key`
3. Type: RSA, Format: `.pem`
4. Download and save `starter-key.pem` securely
5. Set permissions: `chmod 400 starter-key.pem`

---

### Step 2: Create Infrastructure with Terraform

```bash
cd infra/terraform

# Create your local config (NOT committed to git!)
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your values:

```hcl
aws_region    = "eu-central-1"
environment   = "prod"
app_name      = "starter"

# EC2
ec2_instance_type = "t3.small"
ec2_key_name      = "starter-key"        # Name from Step 1

# RDS
db_instance_class = "db.t3.micro"        # Free tier eligible
db_name           = "starter"
db_username       = "postgres"
db_password       = "YourSecurePass123!" # Change this!

# Security - your public IP (find it: curl ifconfig.me)
allowed_ssh_cidr = "123.45.67.89/32"
```

Run Terraform:

```bash
terraform init
terraform plan      # Review changes
terraform apply     # Create resources (type 'yes')
```

Save the outputs:
```
ec2_public_ip      = "12.34.56.78"
db_connection_string = "jdbc:postgresql://xxx.rds.amazonaws.com:5432/starter"
```

---

### Step 3: Configure GitHub Secrets

Go to **GitHub → Your Repo → Settings → Secrets and variables → Actions**

Click **New repository secret** for each:

| Secret Name | Value | Where to get it |
|-------------|-------|-----------------|
| `EC2_HOST` | `12.34.56.78` | Terraform output: `ec2_public_ip` |
| `EC2_USER` | `ec2-user` | Always this value |
| `EC2_SSH_KEY` | Contents of `.pem` file | `cat starter-key.pem` (copy ALL including headers) |
| `DB_URL` | `jdbc:postgresql://xxx.rds.amazonaws.com:5432/starter` | Terraform output: `db_connection_string` |
| `DB_USER` | `postgres` | Same as `db_username` in tfvars |
| `DB_PASSWORD` | `YourSecurePass123!` | Same as `db_password` in tfvars |
| `SWAGGER_USER` | `admin` | Any username you want |
| `SWAGGER_PASSWORD` | `SwaggerSecret123!` | Any password you want |

**⚠️ For `EC2_SSH_KEY`**: Copy the ENTIRE file content including:
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
...
-----END RSA PRIVATE KEY-----
```

---

### Step 4: Deploy

```bash
git push origin master
```

GitHub Actions will automatically:
1. Run tests
2. SSH to EC2
3. Pull code & build Docker images
4. Start the application

---

### Step 5: Verify

After ~5 minutes:

| What | URL |
|------|-----|
| Application | `http://EC2_IP` |
| Swagger UI | `http://EC2_IP/swagger-ui/index.html` |
| Health check | `http://EC2_IP/actuator/health` |

Swagger requires login with `SWAGGER_USER` / `SWAGGER_PASSWORD`.

---

### Connecting to Database (DBeaver)

Use SSH Tunnel:

1. **DBeaver → New Connection → PostgreSQL**
2. **SSH Tab:**
   - Use SSH Tunnel: ✅
   - Host: `EC2_IP`
   - User: `ec2-user`
   - Auth: Private Key → select `.pem` file
3. **Main Tab:**
   - Host: `xxx.rds.amazonaws.com` (RDS endpoint without port)
   - Port: `5432`
   - Database: `starter`
   - User/Password: from tfvars

---

### Costs (estimated)

| Resource | Monthly Cost |
|----------|--------------|
| EC2 t3.small | ~$15 |
| RDS db.t3.micro | $0 (Free Tier) or ~$15 |
| Elastic IP | $0 (when attached) |
| **Total** | **~$15-30/month** |

---

### Destroy Infrastructure

```bash
cd infra/terraform
terraform destroy
```

## 📄 License

MIT
