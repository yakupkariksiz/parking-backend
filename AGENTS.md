# AGENTS.md — Parking Backend

Guidance for AI coding agents working in this repository.

## Project Overview

Spring Boot 3.3.4 / Java 21 parking management system with PostgreSQL 16. Stateless JWT
authentication, two roles (`ROLE_ADMIN`, `ROLE_USER`). Eight vanilla-JS / Tailwind CSS
frontend pages served as static files. Deployed to AWS Lightsail via GitHub Actions CI/CD.

---

## Build & Run Commands

```bash
# Compile only
./mvnw compile

# Run all tests (none exist yet — test infrastructure is in place)
./mvnw test

# Run a single test class
./mvnw test -Dtest=ScanEntryServiceTest

# Run a single test method
./mvnw test -Dtest=ScanEntryServiceTest#shouldReturnResidentOnKnownPlate

# Full build (CI uses this; tests are currently skipped in CI)
./mvnw clean package -DskipTests

# Run locally with dev profile (provides JWT secret default)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Start local PostgreSQL via Docker Compose (required for local dev)
docker compose up -d

# Build Docker image
docker build -t parking-backend .
```

The `JWT_SECRET` env var must be set when running **without** the `dev` profile.
The dev secret lives in `src/main/resources/application-dev.properties` (gitignored).

---

## Project Structure

```
src/main/java/com/example/parking/
    ParkingBackendApplication.java   # Entry point, sets JVM timezone to Europe/Amsterdam
    config/       # SecurityConfig, JwtUtil, JwtAuthenticationFilter, UserInitializer, etc.
    controller/   # @RestController classes (one per domain)
    service/      # Business logic (@Service); controllers delegate here
    repository/   # JpaRepository interfaces; no custom SQL unless necessary
    model/        # JPA @Entity classes
    dto/          # Request/response objects (prefer Java records)
src/main/resources/
    application.properties           # Base config; secrets come from env vars
    application-dev.properties       # Dev-only defaults (gitignored)
    static/                          # Frontend HTML pages + common.js + common.css
```

---

## Java Code Style

### Formatting
- **4-space indentation** — no tabs, in all file types (Java, HTML, JS, CSS).
- **K&R brace style** — opening brace on the same line as the declaration.
- Method-chained calls (e.g., Spring Security DSL) indent each chain level by 8 spaces
  (double indent) relative to the variable.

### Imports
Order (each group separated by a blank line):
1. `com.example.parking.*` (project classes)
2. Third-party libraries (`io.jsonwebtoken`, `org.apache.poi`, etc.)
3. `org.springframework.*`
4. `org.slf4j.*`
5. `jakarta.*`
6. `javax.*`
7. `java.*`

Use wildcard imports only for `jakarta.persistence.*` in entity classes. Prefer individual
imports everywhere else.

### Naming
| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase | `ScanEntryService`, `JwtAuthenticationFilter` |
| Methods / variables | camelCase | `addOrUpdatePlatesForResident`, `licensePlate` |
| Constants | UPPER_SNAKE_CASE | `PARKING_CAPACITY`, `TOKEN_EXPIRY_HOURS` |
| Packages | lowercase singular | `controller`, `service`, `model`, `dto` |
| Logger field | always `log` | `private static final Logger log = ...` |

### Dependency Injection
**Constructor injection only.** Never use `@Autowired` on fields. All dependencies are
declared as `final` fields and injected via an explicit constructor.

### Annotations (ordering)
- Classes: `@Entity` → `@Table`, `@RestController` → `@RequestMapping`,
  `@Configuration` → `@EnableWebSecurity`
- Methods: `@Transactional` → `@Bean`, `@GetMapping` / `@PostMapping` / etc.
- Fields: `@Id` → `@GeneratedValue` → `@Column`, `@ManyToOne` → join column annotations

### DTOs
- **Prefer Java records** for all new DTOs (12 of 13 existing DTOs are records).
- Use traditional POJOs only when computed fields in the constructor are needed
  (see `OccupancyDataPoint`).
- Suffix: `Request` for inbound, `Response` or `Summary` for outbound.

### Entities
Traditional POJOs — **no Lombok** (it is declared in pom.xml but not used; do not introduce
`@Data`, `@Getter`, `@Setter`, or `@Builder`). Write getters and setters manually.
- All IDs are `Long` with `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Timestamps use `java.time.LocalDateTime` set manually in constructors.

### Transactions
- Use `@Transactional` on service methods that write to the database.
- Use `@Transactional(readOnly = true)` on read-only service methods.
- Do not annotate controller methods with `@Transactional`.

### Logging
Use SLF4J. Never use `System.out.println`.
```java
private static final Logger log = LoggerFactory.getLogger(ClassName.class);
// log.info / log.warn / log.error / log.debug
```

---

## Error Handling

Services signal errors by throwing:
- `IllegalArgumentException` → controller catches and returns HTTP 400
- `RuntimeException` → controller catches and returns HTTP 500

Controllers follow this pattern consistently:
```java
try {
    return ResponseEntity.ok(service.doSomething(request));
} catch (IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
} catch (RuntimeException e) {
    return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
}
```

Error responses are always `Map.of("error", message)`.  
Success responses are `Map.of("message", text)` or a DTO object.  
There is no global `@ControllerAdvice` — error handling is inline per controller method.

---

## Frontend Conventions

- **Tailwind CSS via CDN** — no build tooling. Each HTML page includes the CDN script and
  an inline `tailwind.config` block.
- **`common.js`** (loaded at end of `<body>`) provides: `requireAuth()`, `requireAdmin()`,
  `getAuthHeaders()`, `logout()`, `showToast()`, `showSpinner()`, `setBtnLoading()`,
  `renderNavDrawer()`, `loadCurrentUser()`.
- **`common.css`** provides shared component classes: `.btn-primary`, `.btn-danger`,
  `.form-input`, `.data-table`, `.badge-*`, `.modal-overlay`, `.card-hover`, etc.
- **No framework, no bundler.** Vanilla JS with `async/await` and the `fetch()` API.
  Page-specific logic lives in inline `<script>` tags at the bottom of each HTML file.
- All authenticated pages must call `requireAuth()` first, then `renderNavDrawer('pageId')`,
  then `loadCurrentUser()`.
- Admin-only HTML elements use `class="admin-only" style="display:none;"`. JS clears the
  inline style for admins — do NOT add a CSS rule that hides `.admin-only` elements, as
  it will override the JS reveal.
- Nav drawer admin links use `data-admin-only="true" style="display:none;"` instead.

---

## Security & API Conventions

- **Static files** that must be publicly accessible (no auth) must be listed in
  `SecurityConfig.java` inside the `permitAll()` matcher. This includes any new `.css`
  or `.js` files added to the static root.
- **Two roles**: `ROLE_ADMIN` (full access) and `ROLE_USER` (scan only).
- JWT token is stored in `localStorage` as `jwt_token`. Always pass it via
  `getAuthHeaders()` — never hardcode the `Authorization` header.
- API URL conventions: admin endpoints use `/api/admin/` prefix; auth endpoints use
  `/api/auth/`; domain resource endpoints (residents, scan-entries, etc.) do not yet have
  a consistent `/api/` prefix — follow existing patterns per controller.

---

## Git & Deployment

- Single `main` branch. Every push to `main` triggers auto-deploy via GitHub Actions.
- Use SSH remote for pushes involving workflow file changes:
  `git remote set-url origin git@github.com:yakupkariksiz/parking-backend.git`
- **Commit message style** — use a lowercase prefix followed by a colon for bug fixes and
  security changes; use an imperative verb (no prefix) for features:
  - `fix: <short description>` — bug fixes
  - `security: <short description>` — security changes
  - `Add ...` / `Remove ...` / `Replace ...` — feature additions or removals

---

## Known Caveats

- **`CustomOAuth2UserService.java`** is a stale file that was deleted from git but may
  still exist on disk. Ignore any LSP errors it produces.
- **Lombok is declared in pom.xml but is intentionally unused.** Do not introduce Lombok
  annotations.
- **No tests exist yet.** The test infrastructure (`spring-boot-starter-test`, JUnit 5,
  Mockito) is in place. New tests go under `src/test/java/com/example/parking/`.
- **Turkish comments** appear in some older files. Write new comments in English.
- **`ddl-auto=update`** is in use — Hibernate manages the schema automatically. Do not
  add manual DDL SQL unless migrating to Flyway.
