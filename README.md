# legacy-java-migration-practice

## What this app is

A tiny **to-do list service**. It stores to-do items — each one is just a title, plus a
true/false flag for whether it's done — and lets you add, read, change, and delete them.

There is no website or screen. It's an **API**: other programs (or a command-line tool like
`curl`) send it requests over the network, and it sends data back. Think of it as the engine a
to-do app would sit on top of, without the app.

The items are kept in **H2**, a small database that runs inside the app itself and lives in
memory. Nothing to install, but the data disappears when you stop the app.

## Why it exists

It's a practice target for **modernizing old Java code**, which is a large part of real
software maintenance work. Language and framework versions stop getting security updates, so
teams have to upgrade them without changing how the software behaves.

This repo was deliberately built old first — Java 8, Spring Boot 2.7 — and then upgraded. The
git history holds both states, so you can read the "before", the "after", and the exact change
in between.

The upgrade done here is the common one in the Java world today:

| | Before | After |
| --- | --- | --- |
| Language version | Java 8 (2014) | Java 21 |
| Framework | Spring Boot 2.7 | Spring Boot 3.5 |
| Library names | `javax.*` | `jakarta.*` |

The last row looks trivial but is the famous one: Oracle handed Java's enterprise libraries to
the Eclipse Foundation, which couldn't keep the `javax` name for trademark reasons. Every
affected import in every codebase had to be renamed, with no change in behavior.

## How we know the upgrade didn't break anything

The project has a **test suite** — small programs that call the app and check the answers. The
same tests ran before and after the upgrade, with no assertions changed. That's the safety net:
if they pass on both sides, the visible behavior is the same.

Tests only cover what someone thought to write, though, and this repo has a good example of the
gap. See the gotcha below.

## What the API can do

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/todos` | List todos (optional `?completed=true` / `?completed=false`) |
| GET | `/api/todos/{id}` | Fetch one todo (404 when missing) |
| POST | `/api/todos` | Create a todo (201) |
| PUT | `/api/todos/{id}` | Replace title/completed |
| DELETE | `/api/todos/{id}` | Delete a todo (204) |

A title is required and must be 1–255 characters, or the request is rejected with a 400. The
check runs on the raw value, so a whitespace-only title slips through and is stored empty after
the service trims it — `@Size` would have to become `@NotBlank` to close that.

## The migration, step by step

1. **Java 8 → 21:** replaced `maven.compiler.source`/`target` 1.8 with `maven.compiler.release=21` and dropped Java 8 idioms (explicit type arguments, manual unboxing).
2. **Spring Boot 2.7.18 → 3.5.16:** upgraded the parent; Hibernate 6 now infers the H2 dialect, so `spring.jpa.database-platform` was removed and `spring.jpa.open-in-view=false` made explicit.
3. **`javax.*` → `jakarta.*`:** rewrote the `javax.persistence` and `javax.validation` imports.
4. **Tests:** the deprecated `@MockBean` became `@MockitoBean`; no assertions changed.

The package is still named `com.example.legacy` so the before/after diff stays easy to follow.

### Migration gotcha worth studying

Spring MVC 5 treated a trailing slash as an optional path separator; Spring MVC 6 does not, so
`GET /api/todos/` started returning 404 after the upgrade even though no controller code
changed. The test suite passed anyway, because no test used a trailing slash. `WebConfig`
restores the old contract with a `UrlHandlerFilter`, and `TodoControllerTest` now covers both
route shapes. This is the classic shape of a migration regression: behavior changes via a
framework default, not via the diff.

## Project layout

| Concern | Choice |
| --- | --- |
| Build | Maven (`pom.xml` at repo root) |
| Framework | Spring Boot 3.5.16 (web, data-jpa, validation) |
| Persistence | H2 in-memory, Hibernate via Spring Data JPA |
| Tests | JUnit 5 + `spring-boot-starter-test` (MockMvc) |

The code is under `src/main/java/com/example/legacy/`:

- `LegacyApplication` — the entry point that boots the app.
- `Todo` — what a to-do item is (id, title, completed) and the rules its fields must follow.
- `TodoRepository` — talks to the database; Spring writes the queries from the method names.
- `TodoService` — the business logic (trim titles, complain when an id doesn't exist).
- `TodoController` — maps the URLs above onto the service.
- `WebConfig` — the trailing-slash compatibility fix described above.

## How to run it

You need **JDK 21** and **Maven** installed.

```bash
mvn spring-boot:run     # start the app on http://localhost:8080
mvn test                # run the test suite
mvn clean package       # build a runnable jar into target/
```

Once it's running, create a todo and list them back:

```bash
curl -s -X POST http://localhost:8080/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"write migration plan","completed":false}'

curl -s http://localhost:8080/api/todos
```

To see the data as a table, open the database's built-in viewer at
http://localhost:8080/h2-console and connect with JDBC URL `jdbc:h2:mem:legacydb`, user `sa`,
and an empty password. Then run `SELECT * FROM TODOS;`.

Stop the app with `Ctrl+C`. The data is gone on the next start — that's expected with an
in-memory database.
