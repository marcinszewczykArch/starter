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
│   ├── docker-compose.dev.yml  # Dev database (port 5432)
│   ├── docker-compose.test.yml # Test database (port 5433)
│   ├── docker-compose.prod.yml # Production (EC2)
│   ├── Dockerfile.backend      # Backend Docker image (used by CI)
│   ├── Dockerfile.frontend     # Frontend Docker image (used by CI)
│   ├── nginx.conf              # Nginx config (local)
│   ├── nginx.prod.conf         # Nginx config (production)
│   ├── monitoring/             # Monitoring stack config
│   │   ├── prometheus.yml      # Prometheus scrape config
│   │   ├── promtail.yml        # Log collection config
│   │   └── grafana/            # Grafana provisioning
│   └── terraform/              # AWS infrastructure (EC2 Spot)
├── scripts/                    # Utility scripts
│   ├── dev.sh                  # Start dev environment
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

# Stop containers
docker compose -f infra/docker-compose.dev.yml down
docker compose -f infra/docker-compose.test.yml down
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
- `prod` - Production (PostgreSQL in Docker, Swagger protected with Basic Auth)

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
- PostgreSQL 17
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
- GitHub Container Registry (ghcr.io)
- Nginx
- GitHub Actions
- Terraform (AWS EC2 Spot)
- Prometheus + Grafana + Loki (monitoring)

## 🏗️ Production Architecture

```mermaid
graph TB
    subgraph EC2["☁️ AWS EC2 (t3.small Spot)"]
        subgraph App["Application"]
            FE[🌐 Frontend<br/>Nginx :80/:443]
            BE[☕ Backend<br/>Spring Boot :8080]
            DB[(🐘 PostgreSQL<br/>:5432)]
        end
        
        subgraph Mon["Monitoring"]
            PR[📊 Prometheus<br/>metrics]
            GR[📈 Grafana<br/>dashboards]
            LO[📝 Loki<br/>logs]
            PT[📋 Promtail<br/>collector]
        end
        
        FE -->|/api/*| BE
        BE --> DB
        BE -->|/actuator/prometheus| PR
        PR --> GR
        PT -->|container logs| LO
        LO --> GR
    end
    
    U[👤 User] -->|HTTPS :443| FE
    U -->|/grafana| GR
```

## 🚀 Production Deployment

Deploy to AWS EC2 (Spot instance) with automatic CI/CD from GitHub Actions.
PostgreSQL runs in Docker on EC2 (no RDS needed - saves ~$11/month).

### How it works

1. **Push to master** triggers GitHub Actions
2. **CI builds Docker images** and pushes to GitHub Container Registry (`ghcr.io`)
3. **Deploy job SSHs to EC2** and pulls the pre-built images
4. **Docker Compose starts** the application + monitoring stack

No code cloning or building on EC2 - just pull and run!

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

# EC2 Spot Instance (t3.small = 2GB RAM, ~$4.50/month with Spot!)
ec2_instance_type  = "t3.small"
ec2_spot_max_price = "0.015"             # Max hourly price
ec2_key_name       = "starter-key"       # Name from Step 1

# Security - your public IP (find it: curl ifconfig.me)
allowed_ssh_cidr = "123.45.67.89/32"
```

Run Terraform:

```bash
terraform init
terraform plan      # Review changes
terraform apply     # Create resources (type 'yes')
```

Save the output:
```
ec2_public_ip = "12.34.56.78"
```

---

### Step 3: Configure GitHub Secrets

Go to **GitHub → Your Repo → Settings → Secrets and variables → Actions**

Click **New repository secret** for each:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `EC2_HOST` | `12.34.56.78` | Terraform output: `ec2_public_ip` |
| `EC2_USER` | `ec2-user` | Always this value |
| `EC2_SSH_KEY` | Contents of `.pem` file | `cat starter-key.pem` |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `YourSecurePass123!` | Database password |
| `SWAGGER_USER` | `admin` | Swagger UI username |
| `SWAGGER_PASSWORD` | `SwaggerSecret123!` | Swagger UI password |
| `GRAFANA_PASSWORD` | `GrafanaSecret123!` | Grafana admin password |

**⚠️ For `EC2_SSH_KEY`**: Copy the ENTIRE file content including:
```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
...
-----END RSA PRIVATE KEY-----
```

> **Note**: No `DB_URL` needed - PostgreSQL runs locally in Docker on EC2.

---

### Step 4: Deploy

```bash
git push origin master
```

GitHub Actions will automatically:
1. Lint code
2. Run tests
3. Build Docker images and push to `ghcr.io`
4. SSH to EC2, pull images, start containers

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

### Step 6: Configure HTTPS (Optional)

If you have a domain, set up free SSL with Let's Encrypt.

#### 6.1 Configure DNS

Add A records pointing to your EC2 IP:
```
Type: A    Name: @      Value: 12.34.56.78
Type: A    Name: www    Value: 12.34.56.78
```

Wait 5-10 minutes for DNS propagation.

#### 6.2 Get SSL Certificate

```bash
# Connect to EC2
ssh -i ~/.ssh/starter-key.pem ec2-user@12.34.56.78

# Install certbot
sudo yum install -y certbot

# Stop frontend (port 80 must be free)
cd /home/ec2-user/deploy
docker compose stop frontend

# Get certificate (replace with your domain and email!)
sudo certbot certonly --standalone \
  -d yourdomain.com -d www.yourdomain.com \
  --non-interactive --agree-tos \
  -m your-email@example.com

# Start frontend
docker compose start frontend
```

#### 6.3 Update nginx.prod.conf

Update `infra/nginx.prod.conf` with your domain:
```nginx
ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
```

Commit, push, and redeploy.

#### 6.4 Setup Auto-Renewal

Certificates expire after 90 days. Setup automatic renewal:

```bash
# On EC2:
sudo yum install -y cronie
sudo systemctl enable crond
sudo systemctl start crond

# Add cron job
sudo crontab -e
```

Add this line:
```
0 3 * * * certbot renew --quiet && docker exec starter-frontend nginx -s reload
```

This checks daily at 3 AM and renews if needed.

#### 6.5 Verify HTTPS

```bash
# Check certificate dates
sudo openssl x509 -in /etc/letsencrypt/live/yourdomain.com/fullchain.pem -noout -dates
```

Visit `https://yourdomain.com` - you should see 🔒 in the browser!

---

### Connecting to Database (DBeaver)

Database runs in Docker on EC2. Use SSH Tunnel:

1. **DBeaver → New Connection → PostgreSQL**
2. **SSH Tab:**
   - Use SSH Tunnel: ✅
   - Host: `EC2_IP`
   - User: `ec2-user`
   - Auth: Private Key → select `.pem` file
3. **Main Tab:**
   - Host: `localhost`
   - Port: `5432`
   - Database: `starter`
   - User/Password: from GitHub Secrets

---

### Costs (estimated)

| Resource | Monthly Cost |
|----------|--------------|
| EC2 t3.small Spot | ~$4.50 |
| EBS 30GB | ~$2.50 |
| Elastic IP | ~$3.60 |
| Data transfer | ~$1 |
| **Total** | **~$12/month** |

> 💡 Using Spot instances saves ~70% compared to On-Demand!

## 📊 Monitoring

Built-in monitoring stack with Prometheus, Grafana, and Loki.

### Access Grafana

```
URL: https://yourdomain.com/grafana (or http://EC2_IP/grafana without HTTPS)
User: admin
Password: (GRAFANA_PASSWORD from GitHub Secrets)
```

### What's included

| Tool | Purpose | Access |
|------|---------|--------|
| **Grafana** | Dashboards & visualization | `/grafana` |
| **Prometheus** | Metrics collection | Internal |
| **Loki** | Log aggregation | Via Grafana |
| **Promtail** | Log collection | Internal |

### Available Metrics

- **JVM**: Memory, GC, Threads
- **HTTP**: Requests/sec, latency, error rate
- **Database**: Connection pool, query time
- **System**: CPU, disk usage

### Viewing Logs

1. Open Grafana → **Explore**
2. Select **Loki** datasource
3. Query: `{job="containerlogs"}`
4. Filter by container: `{container_id=~".*backend.*"}`

---

### Destroy Infrastructure

```bash
cd infra/terraform
terraform destroy
```

## 📄 License

MIT
