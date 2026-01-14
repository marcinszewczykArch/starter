# ADR-009: Login History and Geolocation Tracking

## Status
Accepted

## Date
2026-01-14

## Context

We need to track user login history for security auditing and admin monitoring. This includes:
- When users logged in
- Where they logged in from (location)
- What device/browser they used
- Failed login attempts

Location data helps detect suspicious activity (e.g., logins from unexpected locations).

## Decision

### 1. Login History Table

Create a `login_history` table that stores:
- Timestamp of login attempt
- Success/failure status with reason
- Location (latitude, longitude) with source indicator
- Country and city (from IP lookup)
- Device info (browser, OS, device type)
- IP address

### 2. Dual Location Strategy

Use a **GPS-first, IP-fallback** approach:

```
User logs in
    ↓
Frontend requests GPS (3s timeout)
    ↓
GPS granted? → Use precise coordinates (source: GPS)
    ↓
GPS denied? → Backend does IP lookup (source: IP)
```

**GPS (Browser Geolocation API):**
- Accuracy: 5-50 meters
- Requires user permission
- Optional - doesn't block login if denied

**IP Geolocation (ip-api.com):**
- Accuracy: 1-10 km (city level)
- No user permission needed
- Free tier: 45 req/min
- Fallback when GPS unavailable

### 3. Device Detection

Parse User-Agent header to extract:
- Device type (desktop, mobile, tablet)
- Browser name and version
- Operating system

### 4. Async Recording

Login history is recorded **asynchronously** using Spring `@Async`:
- Login response is not delayed by history recording
- Failures in recording don't affect login
- IP lookup happens in background

### 5. Admin Interface

- Users list shows `lastLoginAt` column
- Click on user opens modal with:
  - User details
  - Paginated login history
  - Location source indicator (🛰️ GPS, 🌐 IP)
  - Device info

### 6. Failed Attempts Tracking

Record failed logins with:
- Attempted email (even if user doesn't exist)
- Failure reason: `INVALID_PASSWORD`, `USER_NOT_FOUND`
- Same location/device tracking

## Consequences

### Positive
- Security auditing: Track suspicious login patterns
- User support: Debug login issues
- Compliance: Audit trail for access
- Future: Enable maps visualization of logins

### Negative
- Privacy: Stores location data (GPS can be precise)
- External dependency: ip-api.com for IP lookup
- Storage growth: New row per login attempt

### Mitigations
- GPS is opt-in (user must grant permission)
- IP-based location is city-level, not precise
- Old login history can be archived/deleted
- ip-api.com failure doesn't block login

## Technical Details

### Database Schema
```sql
CREATE TABLE login_history (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,
    logged_in_at    TIMESTAMP NOT NULL,
    success         BOOLEAN NOT NULL,
    failure_reason  VARCHAR(50),
    attempted_email VARCHAR(255),
    latitude        DECIMAL(10, 7),
    longitude       DECIMAL(10, 7),
    location_source VARCHAR(10),  -- 'GPS', 'IP'
    country         VARCHAR(100),
    city            VARCHAR(100),
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    device_type     VARCHAR(20),
    browser         VARCHAR(100),
    os              VARCHAR(100)
);
```

### API Endpoints
- `POST /api/auth/login` - Now accepts optional `location: { latitude, longitude }`
- `GET /api/admin/users/{id}/logins` - Paginated login history (admin only)

### Frontend Flow
```typescript
// AuthContext.tsx
const login = async (request: LoginRequest) => {
  // Request GPS if enabled (non-blocking, 3s timeout)
  const location = FEATURES.gpsEnabled ? await requestGpsLocation(3000) : null;
  
  // Include in login request if available
  const response = await authApi.login({ ...request, location });
  // ...
};
```

### Configuration

GPS location request can be disabled via environment variable:

```bash
# In frontend .env or docker-compose
VITE_GPS_ENABLED=false
```

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_GPS_ENABLED` | `true` | Enable browser GPS request on login |

## Alternatives Considered

### 1. GPS Only
- Pros: Most accurate
- Cons: Users often deny permission; mobile-only reliable
- Rejected: Too restrictive

### 2. IP Only
- Pros: Always available
- Cons: City-level accuracy only
- Rejected: Want precise data when available

### 3. Synchronous Recording
- Pros: Guaranteed recording
- Cons: Slower login response
- Rejected: UX over guarantee

### 4. MaxMind GeoLite2 (local DB)
- Pros: No external calls, unlimited requests
- Cons: Need to download/update DB periodically
- Rejected: More complex setup for starter template

## When to Reconsider

- If ip-api.com rate limits become an issue → Switch to MaxMind GeoLite2
- If login_history table grows too large → Add retention policy
- If GPS permission UX is problematic → Make GPS request configurable
- If precise location is a privacy concern → Add user opt-out

