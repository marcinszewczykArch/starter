# 🚀 Onboarding Guide

This guide walks you through setting up a new instance of the Starter application after forking the repository.

## Prerequisites

### Local Development
- **Java 21** (Temurin/OpenJDK recommended)
- **Node.js 20** (LTS)
- **Docker** and **Docker Compose**
- **Git**

### Production Deployment
- **AWS Account** with appropriate permissions
- **Terraform** (v1.5+)
- **Domain** (optional, for custom domain)
- **Resend Account** (for emails)

---

## Part 1: Local Development Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/starter.git
cd starter
```

### Step 2: Start Development Environment

```bash
./scripts/dev.sh
```

This will:
- Start PostgreSQL in Docker (port 5432)
- Run database migrations (Flyway)
- Start backend (port 8080)
- Start frontend (port 5173)

### Step 3: Verify It Works

| URL | What |
|-----|------|
| http://localhost:5173 | Frontend |
| http://localhost:8080/swagger-ui.html | API Docs |
| http://localhost:8080/actuator/health | Health Check |

### Step 4: Test Users

| Email | Password | Role |
|-------|----------|------|
| `admin@starter.com` | `password123` | ADMIN |
| `user@starter.com` | `password123` | USER |

---

## Part 2: Production Setup

### Step 1: AWS Setup

#### 1.1 Create SSH Key Pair

```bash
# In AWS Console: EC2 → Key Pairs → Create Key Pair
# Name: starter-key
# Type: RSA
# Format: .pem

# Download and secure the key
chmod 400 starter-key.pem
```

#### 1.2 Install Terraform

```bash
# macOS
brew install terraform

# Linux
sudo apt-get install terraform

# Verify
terraform --version
```

### Step 2: Configure Terraform

```bash
cd infra/terraform

# Copy example config
cp terraform.tfvars.example terraform.tfvars

# Edit with your values
nano terraform.tfvars
```

**terraform.tfvars:**
```hcl
aws_region         = "eu-central-1"
environment        = "prod"
app_name           = "your-app-name"
ec2_instance_type  = "t3.small"
ec2_spot_max_price = "0.015"
ec2_key_name       = "starter-key"    # Your key pair name
allowed_ssh_cidr   = "YOUR_IP/32"     # Your IP for SSH access
```

### Step 3: Deploy Infrastructure

```bash
# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Deploy (type "yes" to confirm)
terraform apply
```

**Save the outputs:**
```
ec2_public_ip = "12.34.56.78"    # You'll need this!
```

### Step 4: Configure GitHub Secrets

Go to: **GitHub → Your Repo → Settings → Secrets → Actions**

Add these secrets:

| Secret | Value | How to get |
|--------|-------|------------|
| `EC2_HOST` | `12.34.56.78` | Terraform output |
| `EC2_USER` | `ec2-user` | Always this value |
| `EC2_SSH_KEY` | (entire .pem file) | `cat starter-key.pem` |
| `DB_USER` | `postgres` | Your choice |
| `DB_PASSWORD` | `YourSecurePass!` | Generate secure password |
| `JWT_SECRET` | (see below) | `openssl rand -base64 32` |
| `CORS_ALLOWED_ORIGINS` | `https://yourdomain.com` | Your domain |
| `GRAFANA_PASSWORD` | `GrafanaPass123!` | Your choice |
| `RESEND_API_KEY` | `re_xxxxx...` | From Resend dashboard |

**Generate JWT Secret:**
```bash
openssl rand -base64 32
# Example output: Cl+N3RD0LitWw3jREk4ZWQPzIjzIFr1W2gKge4flvlc=
```

### Step 5: Configure Email (Resend)

1. Create account at [resend.com](https://resend.com)
2. Go to **Domains** → **Add Domain**
3. Add DNS records to your domain provider:
   - TXT: `resend._domainkey` (DKIM)
   - TXT: `send` (SPF)
   - MX: `send` (bounce handling)
   - TXT: `_dmarc` (DMARC)
4. Wait for verification
5. Copy API key to GitHub Secrets

### Step 6: Deploy Application

```bash
# Push to master to trigger deployment
git push origin master
```

Monitor deployment: **GitHub → Actions**

### Step 7: Configure HTTPS (Optional but Recommended)

SSH into EC2:
```bash
ssh -i starter-key.pem ec2-user@YOUR_EC2_IP
```

Install certificate:
```bash
# Stop frontend temporarily
cd /home/ec2-user/deploy
docker compose stop frontend

# Get certificate
sudo certbot certonly --standalone \
  -d yourdomain.com \
  --non-interactive --agree-tos \
  -m your-email@example.com

# Start frontend
docker compose up -d frontend
```

### Step 8: Point Domain to EC2

Add DNS A record:
```
Type: A
Name: @ (or subdomain)
Value: YOUR_EC2_IP
TTL: 300
```

---

## Part 3: Verify Everything Works

### Health Checks

| Check | URL |
|-------|-----|
| Frontend | https://yourdomain.com |
| API Health | https://yourdomain.com/actuator/health |
| API Docs | https://yourdomain.com/swagger-ui.html |
| Grafana | https://yourdomain.com/grafana |

### Test Email

1. Register a new account
2. Check email for verification link
3. Click link to verify
4. Login should work

### Test Admin Features

1. Login as admin (update password!)
2. Check Admin Panel
3. View Grafana dashboards

---

## Part 4: Customization

### Change Application Name

1. Update `app_name` in `terraform.tfvars`
2. Update `app.email.app-name` in `application.yml`
3. Update title in `frontend/index.html`
4. Update `name` in `frontend/package.json`

### Change Default Users

Edit `backend/main/src/main/resources/db/migration/V4__seed_users.sql`:
```sql
-- Change emails and regenerate password hashes
INSERT INTO users (email, password, role) VALUES
('admin@yourcompany.com', '$2a$10$...', 'ADMIN'),
('user@yourcompany.com', '$2a$10$...', 'USER');
```

Generate BCrypt hash:
```java
// In any Java test class
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
System.out.println(encoder.encode("your-password"));
```

### Add New Features

See [Architecture Decision Records](./adr/README.md) for architectural context.

---

## Troubleshooting

### Backend won't start
```bash
# Check logs
docker compose logs backend

# Common issues:
# - Database not ready: wait for health check
# - Wrong DB credentials: check .env file
# - Port conflict: check if 8080 is free
```

### Frontend shows blank page
```bash
# Check if backend is healthy
curl http://localhost:8080/actuator/health

# Check frontend logs
docker compose logs frontend
```

### Emails not sending
```bash
# Check if RESEND_API_KEY is set
echo $RESEND_API_KEY

# Check backend logs for email errors
docker compose logs backend | grep -i email
```

### CI/CD failing
1. Check GitHub Actions logs
2. Verify all secrets are set correctly
3. Check EC2 is running and accessible

---

## Getting Help

- **Issues**: Open a GitHub issue
- **ADRs**: Check `docs/adr/` for architectural decisions
- **API Docs**: `/swagger-ui.html`

---

## Quick Reference

### Useful Commands

```bash
# Local dev
./scripts/dev.sh          # Start everything
./scripts/lint.sh         # Run linters
./scripts/test.sh         # Run tests

# Production (on EC2)
cd /home/ec2-user/deploy
docker compose logs -f    # View logs
docker compose restart    # Restart all
docker compose ps         # Check status
```

### Important Files

| File | Purpose |
|------|---------|
| `infra/terraform/` | Infrastructure as Code |
| `.github/workflows/ci.yml` | CI/CD Pipeline |
| `infra/docker-compose.prod.yml` | Production containers |
| `backend/main/src/main/resources/application.yml` | Backend config |
| `docs/adr/` | Architecture decisions |


