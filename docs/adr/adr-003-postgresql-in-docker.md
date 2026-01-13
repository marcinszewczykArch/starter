# ADR 003: PostgreSQL in Docker vs RDS

**Date**: 2026-01-13  
**Status**: Accepted

## Context

The application needs a PostgreSQL database. Options:

1. **PostgreSQL in Docker on EC2** - Self-managed in container
2. **AWS RDS PostgreSQL** - Managed database service
3. **AWS Aurora PostgreSQL** - Enhanced managed PostgreSQL

## Decision

**Run PostgreSQL in Docker on the same EC2 instance as the application.**

```yaml
# docker-compose.prod.yml
services:
  db:
    image: postgres:17-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=starter
      - POSTGRES_USER=${DB_USER}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
```

## Consequences

### Pros
- **Zero additional cost**: No RDS fees (~$15-30/month saved)
- **Low latency**: Same machine, no network hop
- **Simple deployment**: Part of the same Docker Compose
- **Full control**: Any PostgreSQL version, any extension
- **Easy local dev**: Same setup locally and in production

### Cons
- **No automatic backups**: Must implement manually
- **No automatic failover**: Single point of failure
- **Shared resources**: Competes with app for CPU/RAM
- **Manual upgrades**: Must handle PostgreSQL updates yourself

### Risk Mitigation

1. **Data persistence**: Docker volume on EBS (survives container restart)
2. **Spot interruption**: Instance stops (not terminates), volume preserved
3. **Backups**: Implement daily `pg_dump` to S3 (recommended)

## When to Migrate to RDS

Consider RDS when:

| Trigger | Threshold |
|---------|-----------|
| Database size | > 10GB |
| Concurrent connections | > 50 |
| Need point-in-time recovery | Yes |
| Compliance requirements | SOC2, HIPAA |
| Team size | > 3 developers |

## Migration Path to RDS

### Step 1: Create RDS Instance
```hcl
# terraform/rds.tf
resource "aws_db_instance" "postgres" {
  identifier        = "starter-db"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = "db.t3.micro"  # ~$15/month
  allocated_storage = 20
  
  db_name  = "starter"
  username = var.db_user
  password = var.db_password
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  skip_final_snapshot    = true
}
```

### Step 2: Migrate Data
```bash
# Export from Docker PostgreSQL
docker exec starter-db pg_dump -U postgres starter > backup.sql

# Import to RDS
psql -h rds-endpoint.amazonaws.com -U postgres -d starter < backup.sql
```

### Step 3: Update Application
```yaml
# Change connection string
SPRING_DATASOURCE_URL=jdbc:postgresql://rds-endpoint:5432/starter
```

### Step 4: Remove Docker PostgreSQL
```yaml
# docker-compose.prod.yml - remove db service
```

## Cost Comparison

| Option | Monthly Cost |
|--------|--------------|
| PostgreSQL in Docker | $0 (included in EC2) |
| RDS db.t3.micro | ~$15 |
| RDS db.t3.small | ~$30 |
| RDS db.t3.medium | ~$60 |
| Aurora Serverless v2 | ~$50+ |

## References

- [AWS RDS Pricing](https://aws.amazon.com/rds/postgresql/pricing/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)


