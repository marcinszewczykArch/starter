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

**Test Users** (created automatically):
| Email | Password | Role |
|-------|----------|------|
| `admin@starter.com` | `password123` | ADMIN |
| `user@starter.com` | `password123` | USER |

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
│       │       ├── domain/     # Domain entities (User, Example)
│       │       ├── dto/        # Data Transfer Objects
│       │       ├── exception/  # Custom exceptions
│       │       ├── repository/ # Database repositories
│       │       ├── security/   # JWT, filters, UserPrincipal
│       │       └── service/    # Business logic
│       └── src/main/resources/
│           └── db/migration/   # Flyway migrations
├── frontend/                   # React/Vite frontend
│   └── src/
│       ├── api/                # API client + auth API
│       ├── components/         # Shared components (Header, etc.)
│       ├── context/            # React Context (AuthContext)
│       ├── pages/              # Page components (Dashboard, Login, etc.)
│       ├── utils/              # Utility functions
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
│   ├── lint.sh                 # Lint & format (run before commit)
│   └── test.sh                 # Run all tests
├── requests/                   # HTTP client requests (IntelliJ)
├── config/                     # Shared config
│   └── code-format.xml         # Eclipse formatter config
└── .github/
    └── workflows/
        └── ci.yml              # GitHub Actions CI/CD (tests + deploy)
```

## 🛠 Available Commands

### Pre-Commit Workflow ⚠️

**Always run before committing** to ensure CI pipeline will pass:

```bash
# Step 1: Lint & format (fixes issues automatically)
./scripts/lint.sh

# Step 2: Run tests
./scripts/test.sh

# Step 3: Commit
git add -A && git commit -m "your message"
```

This mirrors the CI pipeline: `lint → test → build → deploy`

### Development

```bash
# Start full development environment
./scripts/dev.sh

# Lint & format code (backend + frontend)
./scripts/lint.sh

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

## 🔐 Authentication

The application uses **JWT (JSON Web Tokens)** for authentication with role-based access control.

### Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant DB as Database

    Note over U,DB: Registration
    U->>F: Fill registration form
    F->>B: POST /api/auth/register
    B->>B: Validate email & password
    B->>B: Hash password (BCrypt)
    B->>DB: Save user
    DB-->>B: User created
    B->>B: Generate JWT token
    B-->>F: {token, userId, email, role}
    F->>F: Store token in localStorage
    F-->>U: Redirect to Dashboard

    Note over U,DB: Login
    U->>F: Enter credentials
    F->>B: POST /api/auth/login
    B->>DB: Find user by email
    B->>B: Verify password
    B->>B: Generate JWT token
    B-->>F: {token, userId, email, role}
    F->>F: Store token in localStorage
    F-->>U: Redirect to Dashboard

    Note over U,DB: Authenticated Request
    U->>F: View examples
    F->>B: GET /api/examples<br/>Authorization: Bearer {token}
    B->>B: Validate JWT signature
    B->>B: Extract userId from token
    B->>DB: Query (filtered by userId)
    DB-->>B: User's examples
    B-->>F: JSON response
    F-->>U: Display data
```

### User Roles

| Role | Permissions |
|------|-------------|
| `USER` | View/create own examples only |
| `ADMIN` | View all examples, access Grafana/Logs links |

### Default Test Users

Two users are created automatically via database migration (`V4__seed_users.sql`):

| Email | Password | Role |
|-------|----------|------|
| `admin@starter.com` | `password123` | ADMIN |
| `user@starter.com` | `password123` | USER |

> ⚠️ **Change these passwords in production!**

### JWT Token Structure

The JWT token contains:
- `sub` (subject): User ID
- `email`: User email
- `role`: User role (USER/ADMIN)
- `emailVerified`: Whether email is verified
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp (default: 24 hours)

### Email Verification Flow

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant E as Email (Resend)

    U->>F: Register with email
    F->>B: POST /api/auth/register
    B->>B: Create user (emailVerified=false)
    B->>E: Send verification email
    B-->>F: {token, emailVerified: false}
    F-->>U: Show "Check your email"
    
    Note over U: User clicks link in email
    U->>F: GET /verify-email?token=xxx
    F->>B: POST /api/auth/verify-email
    B->>B: Mark email verified
    B-->>F: Success
    F-->>U: "Email verified! Go to login"
    
    U->>F: Login
    F->>B: POST /api/auth/login
    B-->>F: {token, emailVerified: true}
    F-->>U: Redirect to Dashboard
```

### Password Reset Flow

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant E as Email (Resend)

    U->>F: Click "Forgot password?"
    F->>B: POST /api/auth/forgot-password
    B->>B: Generate reset token (1h expiry)
    B->>E: Send reset email
    B-->>F: "If email exists, link sent"
    
    Note over U: User clicks link in email
    U->>F: GET /reset-password?token=xxx
    F-->>U: Show new password form
    
    U->>F: Submit new password
    F->>B: POST /api/auth/reset-password
    B->>B: Validate token, update password
    B-->>F: Success
    F-->>U: "Password changed! Redirecting..."
    F->>F: Redirect to /login
```

### API Endpoints

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/register` | POST | ❌ | Register new user |
| `/api/auth/login` | POST | ❌ | Login and get token |
| `/api/auth/me` | GET | ✅ | Get current user info |
| `/api/auth/verify-email` | POST | ❌ | Verify email with token |
| `/api/auth/resend-verification` | POST | ❌ | Resend verification email |
| `/api/auth/forgot-password` | POST | ❌ | Request password reset |
| `/api/auth/reset-password` | POST | ❌ | Reset password with token |
| `/api/examples` | GET | ✅ | List examples (filtered by user) |
| `/api/examples` | POST | ✅ | Create new example |

### Frontend Authentication

The frontend uses React Context (`AuthContext`) to manage authentication state:

```tsx
// Use in any component
const { user, isAuthenticated, isAdmin, login, logout } = useAuth();

// Protected routes
<ProtectedRoute>
  <Dashboard />
</ProtectedRoute>

// Admin-only content
<AdminOnly>
  <Link to="/admin">Admin Panel</Link>
</AdminOnly>
```

**Token handling:**
- Token stored in `localStorage` as `auth_token`
- Automatically included in API requests via `Authorization: Bearer` header
- Auto-logout on 401 response (expired/invalid token)
- Token verified on page load via `/api/auth/me`

### Row-Level Security

Examples are filtered based on the authenticated user:
- **USER role**: Only sees examples where `user_id = current_user_id`
- **ADMIN role**: Sees all examples in the system

New examples are automatically assigned to the creating user.

## 🛡️ Rate Limiting

Rate limiting is handled by **nginx** (no application code needed).

### Limits

| Endpoint | Limit | Burst | Purpose |
|----------|-------|-------|---------|
| `/api/auth/*` | 1 req/sec | 3 | Brute-force protection |
| `/api/*` | 10 req/sec | 20 | General API protection |

### How it works

- Limits are **per IP address**
- Exceeding the limit returns **429 Too Many Requests**
- Burst allows short spikes (e.g., double-click on login)

### Auth endpoint protection

```
1 req/sec = 60 attempts/min = 3600/hour
→ Brute-force attacks become impractical
```

### Configuration

Rate limits are defined in `infra/nginx.prod.conf`:

```nginx
# Auth: 1 req/sec (strict)
limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=1r/s;

# API: 10 req/sec (general)
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
```

To change limits, modify the `rate=` value and redeploy.

## ⚙️ Configuration

### Backend Profiles

- `local` - Local development (PostgreSQL on port 5432)
- `test` - Testing (PostgreSQL on port 5433)
- `prod` - Production (PostgreSQL in Docker, rate limiting enabled)

### Environment Variables

#### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | - | Active Spring profile |
| `SPRING_DATASOURCE_URL` | - | Database URL |
| `SPRING_DATASOURCE_USERNAME` | postgres | DB username |
| `SPRING_DATASOURCE_PASSWORD` | postgres | DB password |
| `JWT_SECRET` | (dev default) | JWT signing secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | 86400000 | Token expiration (24h default) |
| `CORS_ALLOWED_ORIGINS` | localhost:5173,3000 | Allowed CORS origins |

#### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_BASE_URL` | (empty) | API base URL |

## 📝 Tech Stack

### Backend
- Java 21
- Spring Boot 3.2
- Spring Security + JWT (jjwt)
- Spring JDBC (JdbcClient)
- PostgreSQL 17
- Flyway
- SpringDoc OpenAPI (Swagger)
- Spotless + Error Prone + NullAway

### Frontend
- React 18
- TypeScript
- React Router DOM
- Tailwind CSS
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
| `JWT_SECRET` | (generate - see below) | JWT signing secret (min 32 chars) |
| `CORS_ALLOWED_ORIGINS` | `https://yourdomain.com` | Allowed CORS origins (comma separated) |
| `GRAFANA_PASSWORD` | `GrafanaSecret123!` | Grafana admin password |
| `RESEND_API_KEY` | `re_xxxxx...` | Resend API key for emails |

**🔑 Generate `JWT_SECRET`:**

```bash
# Run this command to generate a secure random key:
openssl rand -base64 32
```

Example output: `Cl+N3RD0LitWw3jREk4ZWQPzIjzIFr1W2gKge4flvlc=`

Copy the entire output (including `=` at the end) and paste as the secret value.

> ⚠️ **Never use example values from documentation!** Always generate your own unique key.

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

Swagger UI is publicly accessible. To execute API calls, click "Authorize" and enter your JWT token.

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
