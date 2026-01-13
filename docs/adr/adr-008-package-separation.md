# ADR-008: Package Separation (Core vs Feature)

## Status

**Accepted** - January 2026

## Context

As the application grows, mixing framework/skeleton code with business logic creates several problems:

1. **Cognitive load** - Developers must understand the entire codebase to add a feature
2. **Merge conflicts** - Multiple features touch the same directories
3. **Unclear boundaries** - Hard to know what's "starter template" vs "my app code"
4. **Onboarding friction** - New developers don't know where to add code

We needed a structure that clearly separates:
- **Framework code** (authentication, security, admin panel) that rarely changes
- **Business features** (the actual app functionality) that changes frequently

## Decision

We adopted a **package separation** approach with three layers:

### Backend (`com.starter.*`)

```
com.starter/
├── core/           # Framework skeleton
│   ├── auth/       # Login, register, password reset, email verification
│   ├── user/       # User entity, repository, service
│   ├── admin/      # Admin panel endpoints
│   ├── email/      # Email service (Resend integration)
│   ├── security/   # JWT, filters, UserPrincipal
│   ├── config/     # Spring configuration classes
│   ├── exception/  # Global exception handlers
│   └── common/dto/ # Shared DTOs (MessageResponse, ErrorResponse)
├── feature/        # Business features
│   └── example/    # Example feature (template for new features)
│       ├── ExampleController.java
│       ├── ExampleService.java
│       ├── ExampleRepository.java
│       ├── Example.java (entity)
│       └── dto/
└── shared/         # Cross-cutting utilities (if needed)
```

### Frontend (`src/*`)

```
src/
├── core/           # Framework skeleton
│   ├── auth/       # Login, register, context, protected routes
│   ├── admin/      # Admin panel components
│   ├── user/       # Dashboard, user pages
│   └── common/     # Header, Logo, MetricCard
├── features/       # Business features
│   └── example/    # Example feature
│       ├── api/
│       ├── components/
│       └── pages/
└── shared/         # API client, types, utilities
    ├── api/
    └── utils/
```

### Test Structure

Tests mirror the main source structure:

```
test/java/com/starter/
├── core/auth/AuthServiceTest.java
├── core/auth/AuthControllerIntegrationTest.java
├── feature/example/ExampleServiceTest.java
└── feature/example/ExampleControllerIntegrationTest.java
```

## Consequences

### Positive

1. **Clear boundaries** - Developers know immediately where to add new code
2. **Reduced cognitive load** - Only need to understand `feature/` for business work
3. **Better onboarding** - New devs can focus on `feature/` first
4. **Cleaner diffs** - Framework updates don't mix with feature changes
5. **Template reusability** - The `example` feature serves as a copy-paste template

### Negative

1. **Deeper import paths** - `import com.starter.core.auth.dto.AuthResponse` vs `import com.starter.dto.AuthResponse`
2. **More directories** - File tree is deeper (but clearer)
3. **Refactoring effort** - Required moving 40+ files and updating imports

### Neutral

1. **No multi-module Gradle** - We chose package separation over Gradle modules for simplicity
2. **Convention over enforcement** - Package rules aren't enforced by tooling (could add ArchUnit later)

## Alternatives Considered

### 1. Multi-Module Gradle

```
backend/
├── core/           # Separate Gradle module
├── feature-maps/   # Separate module per feature
└── shared/
```

**Rejected because:**
- Added build complexity
- Circular dependency management needed
- Overkill for current project size

### 2. Flat Structure (Status Quo)

Keep everything in `controller/`, `service/`, `repository/`.

**Rejected because:**
- Didn't solve the separation problem
- Would get messier as features grow

### 3. Vertical Slicing Only

Group by feature without core/shared distinction.

**Rejected because:**
- Auth code would be scattered across features
- No clear "skeleton" for forking

## When to Reconsider

- If we need **independent deployability** → consider multi-module
- If teams want **strict isolation** → add ArchUnit rules
- If project grows significantly → consider microservices

## References

- [Package by Feature](https://phauer.com/2020/package-by-feature/)
- [Screaming Architecture](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html)

