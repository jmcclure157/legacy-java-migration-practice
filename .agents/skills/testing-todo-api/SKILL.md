---
name: testing-todo-api-h2
description: Exercise the legacy-java-migration-practice REST API and visually verify Hibernate persistence in its embedded H2 console.
---

# Todo API runtime testing

## Devin Secrets Needed
None for the local default configuration. H2 uses local development user `sa`
with an empty password; confirm application.properties before connecting.

## Setup
- Use the repository blueprint for the JDK, Maven mirror, and dependency setup.
- Run `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run`
  from the checkout and capture stdout/stderr to a temporary log.
- Wait for `Started LegacyApplication` and port 8080 readiness.
- This project has no frontend and the default REST API has no authentication.
  Exercise `/api/todos` using direct HTTP, preserving request bodies, response
  statuses, and response bodies as text evidence.

## Browser-visible persistence evidence
- Open `http://localhost:8080/h2-console`.
- Replace the default JDBC URL with `jdbc:h2:mem:legacydb`, then Connect.
  Do not use the default file-backed `jdbc:h2:~/test` URL: it is not the API DB.
- In the SQL textarea enter
  `SELECT ID, TITLE, COMPLETED FROM TODOS ORDER BY ID;` and click Run.
- Create distinct completed/incomplete records via the API, query the console,
  update title and completion via API, click Run again, delete via API, and
  click Run again. Match generated IDs and exact field values at each stage.
- The console uses frames; native screenshot/coordinate actions work. Browser
  Ctrl+= zoom makes the small console text readable in recordings.
- Record the browser queries and annotate the API mutation being checked;
  shell-only HTTP calls should retain text logs, not an idle-desktop recording.

## Important assertions and cleanup
- Check both completion filters before and after changing a record's boolean.
- POST empty/missing/256-character titles should return 400; a 255-character
  title should persist. Check malformed JSON, nonnumeric IDs, and missing IDs.
- Validation/parsing failures may produce handled WARN entries; distinguish
  those from ERROR entries and actual stack traces.
- Delete only test-created IDs. H2 is in-memory: restarting the application
  resets data, so finish console evidence before stopping it.
