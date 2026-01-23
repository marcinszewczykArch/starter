# ADR-010: File Storage with AWS S3

## Status
**Accepted**

## Date
2026-01-15

## Context

Users need to upload and manage files. Options:
1. **Store in PostgreSQL (BYTEA)** - Limited scalability, expensive storage
2. **Store in filesystem** - Not scalable, backup issues, single server dependency
3. **Store in AWS S3** - Scalable, reliable, cost-effective

## Decision

**Use AWS S3 for file storage with:**
- Metadata in PostgreSQL (fast queries, relationships)
- Files in S3 (scalable storage, unlimited capacity)
- Presigned URLs for downloads (no backend bandwidth)
- 1GB limit per user (enforced by SUM query)
- SELECT FOR UPDATE to prevent race conditions

## Architecture

```
┌─────────┐     ┌──────────┐     ┌─────────┐
│ Frontend│────►│ Backend  │────►│PostgreSQL│ (metadata)
└─────────┘     └──────────┘     └─────────┘
                       │
                       ▼
                  ┌─────────┐
                  │   S3    │ (files)
                  └─────────┘
```

### Flow:
1. **Upload**: User uploads file → Backend validates → Save metadata to DB → Upload to S3
2. **List**: Backend queries PostgreSQL for metadata → Returns list to frontend
3. **Download**: Backend generates presigned URL → Frontend downloads directly from S3

## Implementation Details

### Atomicity Strategy

**Upload: DB first, then S3**
- If S3 fails → rollback DB (no orphaned records)
- Atomicity guaranteed by `@Transactional`

**Delete: DB first, then S3**
- User sees immediate success (better UX)
- If S3 fails → orphaned file in S3 (acceptable, invisible to user)

### Race Condition Prevention

- **SELECT FOR UPDATE** in `checkQuotaWithLock()` prevents concurrent uploads from exceeding quota
- Lock held until transaction commits

### Security

- **Filename sanitization**: Prevents path traversal attacks
- **Content-type validation**: Whitelist of allowed MIME types
- **Private bucket**: Files accessible only via presigned URLs
- **User isolation**: Files stored in `users/{userId}/files/` prefix

### Storage Tracking

- **SUM on demand**: `SELECT SUM(size_bytes) FROM user_files WHERE user_id = ?`
- No separate `user_storage_usage` table (simpler, always accurate)
- Index on `(user_id, size_bytes)` for performance

## Consequences

### Positive
- ✅ Scalable storage (unlimited files)
- ✅ Lower costs than database storage
- ✅ Fast metadata queries from PostgreSQL
- ✅ No backend bandwidth for downloads (presigned URLs)
- ✅ Reliable (S3 99.99% availability)

### Negative
- ⚠️ Requires AWS account
- ⚠️ Slightly more complex than database storage
- ⚠️ SUM query performance degrades with many files (acceptable for <1000 files)
- ⚠️ Orphaned files possible if S3 delete fails (acceptable, invisible to user)

### Trade-offs

| Aspect | Choice | Rationale |
|--------|--------|-----------|
| Storage tracking | SUM on demand | Simpler, always accurate, acceptable performance |
| Cleanup orphaned files | Skipped | Low cost, invisible to user, can add later if needed |
| Delete strategy | DB first | Better UX (immediate success) |

## Alternatives Considered

1. **Tabela user_storage_usage**: Rejected - adds complexity, can get out of sync
2. **PostgreSQL trigger**: Considered but rejected - SUM on demand is simpler
3. **Cleanup scheduled job**: Skipped - orphaned files are acceptable

## Migration Path

1. Deploy Terraform (S3 bucket + IAM)
2. Run Flyway migration (user_files table)
3. Deploy backend with file storage feature
4. Deploy frontend with Files page

## References

- AWS S3 Pricing: https://aws.amazon.com/s3/pricing/
- Spring Retry: https://docs.spring.io/spring-retry/docs/current/reference/html/
- PostgreSQL SELECT FOR UPDATE: https://www.postgresql.org/docs/current/sql-select.html#SQL-FOR-UPDATE-SHARE
