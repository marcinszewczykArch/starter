# Architecture Decision Records (ADR)

This directory contains Architecture Decision Records for the Starter application.

## Index

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-001](adr-001-single-ec2-deployment.md) | Single EC2 Deployment | Accepted |
| [ADR-002](adr-002-email-provider-resend.md) | Email Provider (Resend) | Accepted |
| [ADR-003](adr-003-postgresql-in-docker.md) | PostgreSQL in Docker | Accepted |
| [ADR-004](adr-004-jwt-authentication.md) | JWT Authentication | Accepted |
| [ADR-005](adr-005-monitoring-stack.md) | Monitoring Stack | Accepted |
| [ADR-006](adr-006-ci-cd-github-actions.md) | CI/CD with GitHub Actions | Accepted |
| [ADR-007](adr-007-spot-instances.md) | EC2 Spot Instances | Accepted |
| [ADR-008](adr-008-package-separation.md) | Package Separation (Core vs Feature) | Accepted |
| [ADR-009](adr-009-login-history-geolocation.md) | Login History and Geolocation Tracking | Accepted |
| [ADR-010](adr-010-file-storage-s3.md) | File Storage with AWS S3 | Accepted |

## What is an ADR?

An Architecture Decision Record captures an important architectural decision made along with its context and consequences.

## Template

Each ADR follows this structure:
- **Status**: Proposed / Accepted / Deprecated / Superseded
- **Context**: What is the issue we're facing?
- **Decision**: What did we decide?
- **Consequences**: What are the trade-offs?
