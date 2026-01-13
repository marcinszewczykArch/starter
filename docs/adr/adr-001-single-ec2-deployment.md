# ADR 001: Single EC2 Deployment Strategy

**Date**: 2026-01-13  
**Status**: Accepted

## Context

We need to deploy a full-stack application (Spring Boot backend, React frontend, PostgreSQL database, monitoring stack) in a cost-effective way for early-stage development and low-traffic production use.

Options considered:
1. **Single EC2 with Docker Compose** - All services on one machine
2. **AWS ECS/Fargate** - Managed container orchestration
3. **Kubernetes (EKS)** - Full container orchestration
4. **Separate services** - EC2 for app, RDS for database, etc.

## Decision

**Use a single EC2 instance running all services via Docker Compose.**

Architecture:
```
┌─────────────────────────────────────────────┐
│                 EC2 (t3.small)              │
│  ┌─────────┐ ┌─────────┐ ┌──────────────┐  │
│  │ Nginx   │ │ Backend │ │  PostgreSQL  │  │
│  │(frontend)│ │ (Java)  │ │   (Docker)   │  │
│  └─────────┘ └─────────┘ └──────────────┘  │
│  ┌─────────┐ ┌─────────┐ ┌──────────────┐  │
│  │Prometheus│ │ Grafana │ │ Loki+Promtail│  │
│  └─────────┘ └─────────┘ └──────────────┘  │
└─────────────────────────────────────────────┘
```

## Consequences

### Pros
- **Very low cost**: ~$8-10/month total (Spot instance)
- **Simple deployment**: Single `docker compose up`
- **Easy debugging**: Everything in one place, SSH access
- **Fast iteration**: No complex orchestration to manage
- **Good enough**: Handles 100s of concurrent users easily

### Cons
- **Single point of failure**: If EC2 dies, everything dies
- **Limited scalability**: Vertical scaling only (bigger instance)
- **No zero-downtime deploys**: Brief downtime during updates
- **Resource contention**: All services share CPU/RAM

### Mitigations
- Elastic IP ensures stable address after restarts
- Docker volumes preserve data across container restarts
- Spot instance with "stop" behavior (not terminate) preserves data
- Daily backups recommended (see future ADR)

## Scaling Path

When traffic grows, migrate in this order:

### Stage 1: Vertical Scaling (0-1000 users)
```
t3.small (2GB) → t3.medium (4GB) → t3.large (8GB)
```
Cost: $8 → $30 → $60/month

### Stage 2: Extract Database (1000-10000 users)
```
┌──────────────┐      ┌──────────────┐
│     EC2      │ ───► │    RDS       │
│ (App + Mon.) │      │ (PostgreSQL) │
└──────────────┘      └──────────────┘
```
See: ADR-008 (future)

### Stage 3: Horizontal Scaling (10000+ users)
```
┌─────────┐     ┌──────────────┐     ┌─────────┐
│   ALB   │ ──► │   EC2 (x2+)  │ ──► │   RDS   │
└─────────┘     └──────────────┘     └─────────┘
```
See: ADR-009, ADR-010 (future)

### Stage 4: Container Orchestration (100000+ users)
Consider ECS or Kubernetes when:
- Need auto-scaling based on load
- Multiple microservices
- Complex deployment patterns

## References

- [AWS Well-Architected: Cost Optimization](https://docs.aws.amazon.com/wellarchitected/latest/cost-optimization-pillar/)
- [The Twelve-Factor App](https://12factor.net/)


