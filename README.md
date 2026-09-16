# legacy-java-migration-practice

A small todo REST API, originally scaffolded as an **intentionally legacy** Java 8 / Spring Boot 2.7 app and now
modernized. It exists as a worked example of a Java modernization: the git history holds the "before" state and
the migration commit, and the test suite is unchanged across both to prove behavior stayed the same.

Current state:

- Java 21 (`maven.compiler.release=21`).
- Spring Boot 3.5.x.
- `jakarta.persistence.*` JPA annotations and `jakarta.validation.*` constraints.
- In-memory H2 datasource with the H2 console enabled.

## Stack

| Concern | Choice |
| --- | --- |
| Build | Maven (`pom.xml` at repo root) |
| Framework | Spring Boot 3.5.16 (web, data-jpa, validation) |
| Persistence | H2 in-memory, Hibernate via Spring Data JPA |
| Tests | JUnit 5 + `spring-boot-starter-test` (MockMvc) |

## Build and run

Requires JDK 21.

```bash
mvn spring-boot:run     # start the app on http://localhost:8080
mvn test                # run the test suite
mvn clean package       # build the executable jar into target/
```

H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:legacydb`, user `sa`, empty password).

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/todos` | List todos (optional `?completed=true` / `?completed=false`) |
| GET | `/api/todos/{id}` | Fetch one todo (404 when missing) |
| POST | `/api/todos` | Create a todo (201) |
| PUT | `/api/todos/{id}` | Replace title/completed |
| DELETE | `/api/todos/{id}` | Delete a todo (204) |

Example:

```bash
curl -s -X POST http://localhost:8080/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"write migration plan","completed":false}'
```

## Migration performed

1. **Java 8 → 21:** replaced `maven.compiler.source`/`target` 1.8 with `maven.compiler.release=21` and dropped Java 8 idioms (explicit type arguments, manual unboxing).
2. **Spring Boot 2.7.18 → 3.5.16:** upgraded the parent; Hibernate 6 now infers the H2 dialect, so `spring.jpa.database-platform` was removed and `spring.jpa.open-in-view=false` made explicit.
3. **`javax.*` → `jakarta.*`:** rewrote the `javax.persistence` and `javax.validation` imports.
4. **Tests:** the deprecated `@MockBean` became `@MockitoBean`; no assertions changed, and all 9 tests still pass.

The package is still named `com.example.legacy` so the before/after diff stays easy to follow.

### Migration gotcha worth studying

Spring MVC 5 treated a trailing slash as an optional path separator; Spring MVC 6 does not, so `GET /api/todos/`
started returning 404 after the upgrade even though no controller code changed. `WebConfig` restores the old
contract with a `UrlHandlerFilter`, and `TodoControllerTest` now covers both route shapes. This is the classic
shape of a migration regression: behavior changes via a framework default, not via the diff.
