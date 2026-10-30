# CLAUDE.md — RestApiFramework

REST API test automation framework built with Java, REST Assured and Maven, for the Rahul Shetty Academy
Place API. Study project, committed step by step: each piece arrives through its own `feature/*` branch and
pull request.

- Coding standards: the `best-practices` skill (`.claude/skills/best-practices/SKILL.md`). Follow it for every
  POJO, utility, assertion, logger call, config value or endpoint.
- Reviews: `/test-review` (all framework code) or `/test-review <file>`.

## Rules for Claude (most important)

- **Never change code or files without my consent.** Show the proposed change (diff or full snippet) and explain
  why; edit only after I explicitly say yes.
- **Ask before running anything that calls the API.** It is a live public API, and calls create and delete real data.
- Never commit secrets or real personal data. The repository is public; test data must be clearly fake.
- Don't add or upgrade dependencies without asking.
- After a change, tell me how to verify it.
- When we make a decision, propose a line for the Decisions log below.

## Environment

- Java 11 or newer (logback-classic requires it), Maven, IntelliJ IDEA.
- `mvn` is not on the PATH on my machine yet: run Maven from IntelliJ's **Maven tool window → Lifecycle**.
  The `mvn` command below works once Maven is installed.
- Run from the project root: `Utils` locates `Global.properties` relative to the folder Maven is started from.

## Commands

- Compile everything, including test code: `mvn test-compile` (should end with BUILD SUCCESS)

## Layout

- `src/main/java/Pojo/` request models (`AddPlace`, `Location`), serialized by Jackson
- `src/test/java/Resources/` `Global.properties` (settings), `GlobalEnum` (endpoints),
  `Utils` (request spec, properties reader, JSON helper)
- `src/test/java/Assertion/Assertions` logging assertion: `assertEquals(description, expected, actual)`
- `src/test/java/Logger/Log` shared logger: `logInformation`, `logWarning`, `logError`
- `src/test/resources/logback-test.xml` log format and outputs
- Generated, never edit or commit: `target/`, `requestLogs.txt`

## Conventions (summary; the skill has the full detail)

- Methods and variables camelCase; constants and enum constants UPPER_SNAKE_CASE; properties keys
  dot-separated lowercase (`api.base.uri`); packages keep the existing PascalCase style.
- Settings only in `Global.properties`, read with `Utils.getPropertiesValue`; endpoints only in `GlobalEnum`.
- Every request starts from `given().spec(requestSpecification())`.
- Verify only with `Assertions.assertEquals`, expected before actual, with a specific `Verify ...` description.
- Log with `Log`; never `System.out`, never secrets.
- Javadoc on every class and public method; single-class imports; format with Ctrl+Alt+L.

## Gotchas

- The request specification is built once per run and cached, so every request logs to the same
  `requestLogs.txt`, which is overwritten at the start of each run.
- `getJsonPath` returns `null` for a missing JSON path (and logs a warning); a `null` in an assertion usually
  means a wrong path or an error response.
- `getPropertiesValue` returns `null` for a missing key (and logs a warning); check the key's spelling and case first.
- `maven-surefire-plugin` has `testFailureIgnore = true` in `pom.xml`, so once tests exist the build will report
  **BUILD SUCCESS even when tests fail**. Always check the test results, not just the build status.

## Decisions log

Add one line per decision so future sessions keep it.

- 2026-10-28: Framework is committed step by step, one `feature/*` branch and pull request per piece.
- 2026-10-29: Logging uses SLF4J + Logback; assertions log PASS/FAIL through `Assertions.assertEquals`.
- 2026-10-29: Methods renamed to Java camelCase (`logInformation`, `assertEquals`, `getPropertiesValue`, ...).
- 2026-10-30: Enum constants use UPPER_SNAKE_CASE without an `API` suffix (`ADD_PLACE`).
- 2026-10-30: Properties keys are dot-separated lowercase (`api.base.uri`, `api.key`).
- 2026-10-30: Packages keep the existing PascalCase names (`Pojo`, `Resources`, `Assertion`, `Logger`).
- 2026-10-30: Created this CLAUDE.md and the `best-practices` and `test-review` skills.