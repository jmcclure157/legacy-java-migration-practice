# legacy-java-migration-practice

An **intentionally legacy** Java 8 / Spring Boot 2.7 REST application used as the "before" state for practicing
Java modernization. Nothing here should be modernized in place until a migration exercise calls for it.

Why it is legacy on purpose:

- Java 8 (`source`/`target` 1.8), no Java 9+ language features or APIs.
- Spring Boot 2.7.18 (the last 2.x line), which still targets the `javax.*` namespace.
- `javax.persistence.*` JPA annotations and `javax.validation.*` constraints.
- In-memory H2 datasource with the H2 console enabled.

## Stack

| Concern | Choice |
| --- | --- |
| Build | Maven (`pom.xml` at repo root) |
| Framework | Spring Boot 2.7.18 (web, data-jpa, validation) |
| Persistence | H2 in-memory, Hibernate via Spring Data JPA |
| Tests | JUnit 5 + `spring-boot-starter-test` (MockMvc) |

## Build and run

Requires a JDK 8 toolchain (a newer JDK will also compile with `-source/-target 1.8`).

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

## Intended future migration path

1. **Java 8 → 17 (then 21):** bump `java.version`, replace legacy idioms, verify no removed JDK APIs are used.
2. **Spring Boot 2.7 → 3.x:** upgrade the parent, adopt the Boot 3 configuration and Hibernate 6 changes.
3. **`javax.*` → `jakarta.*`:** rewrite `javax.persistence` and `javax.validation` imports to their Jakarta EE 9+ equivalents.

The tests in `src/test/java/com/example/legacy/` exist to prove behavior is unchanged after each of those steps.
