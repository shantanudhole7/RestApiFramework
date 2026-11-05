# CLAUDE.md — RestApiFramework

REST API test automation framework built with Java, REST Assured, Cucumber and Maven, for the Rahul Shetty
Academy Place API. Study project, committed step by step: each piece arrives through its own `feature/*` branch
and pull request.

- Coding standards: the `best-practices` skill (`.claude/skills/best-practices/SKILL.md`). Follow it for every
  POJO, utility, assertion, logger call, config value, endpoint, feature file or step definition.
- Reviews: `/test-review` (all tests and framework code) or `/test-review <file>`.

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
  The `mvn` commands below work once Maven is installed.
- Run from the project root: `Utils` locates `Global.properties`, and `TestRunner` locates the feature files,
  relative to the folder Maven is started from.

## Commands

- Compile everything, including test code: `mvn test-compile` (should end with BUILD SUCCESS)
- Run all scenarios: `mvn test`, or run `TestRunner` in IntelliJ (green arrow next to the class)
- Run by tag: `mvn test -Dcucumber.filter.tags="@AddPlace"`
- Run one scenario: green arrow next to it in the `.feature` file

## Layout

- `src/main/java/Pojo/` request models (`AddPlace`, `Location`), serialized by Jackson
- `src/test/java/Resources/` `Global.properties` (settings), `GlobalEnum` (endpoints),
  `TestDataBuilder` (payloads), `Utils` (request/response specs, properties reader, JSON helper)
- `src/test/java/Assertion/Assertions` logging assertions: `assertEquals(description, expected, actual)`,
  `assertHttpOk(response)`
- `src/test/java/Logger/Log` shared logger: `logInformation`, `logWarning`, `logError`
- `src/test/java/Features/` Cucumber feature files (`PlaceApi.feature`)
- `src/test/java/StepDefinition/StepDefinition` step definitions for the feature files
- `src/test/java/cucumber/Options/TestRunner` JUnit runner: features path, glue, `pretty` and JSON report plugins
- `src/test/resources/logback-test.xml` log format and outputs
- Generated, never edit or commit: `target/`, `requestLogs.txt`

## Conventions (summary; the skill has the full detail)

- Methods and variables camelCase; constants and enum constants UPPER_SNAKE_CASE; properties keys
  dot-separated lowercase (`api.base.uri`); packages keep the existing PascalCase style.
- Settings only in `Global.properties`, read with `Utils.getPropertiesValue`; endpoints only in `GlobalEnum`.
- Every request starts from `given().spec(defaultRequestSpecification())`; every response is checked with
  `.then().spec(defaultResponseSpecification())` (JSON content only; status codes are checked in the steps).
- Verify only with `Assertions.assertEquals` / `assertHttpOk`, expected before actual, with a specific
  `Verify ...` description.
- Log with `Log`; never `System.out`, never secrets.
- Javadoc on every class and public method; single-class imports; format Java files with Ctrl+Alt+L, never `.md` files.
- POJO fields camelCase; map snake_case JSON keys with `@JsonProperty("json_key")`.
- Feature files PascalCase, one per API area; tags PascalCase (`@PlaceApi`, `@AddPlace`, `@Smoke`).
- Step definition annotations match the feature text exactly; unimplemented steps throw `PendingException`,
  never an empty body.
- Step definitions need Javadoc only when their behaviour isn't obvious from the step text.

## Gotchas

- `maven-surefire-plugin` has `testFailureIgnore = true` in `pom.xml`, so the build reports **BUILD SUCCESS even
  when scenarios fail**. Always check the test results or the Cucumber report, not just the build status.
- Cucumber links steps by exact text: changing a word in the feature (`succeed` → `succeeds`) without changing the
  annotation turns the step **undefined**.
- An empty step body **passes**. Keep `throw new PendingException()` in a step until it is implemented.
- Feature files name endpoints as strings (`"ADD_PLACE"`) resolved with `GlobalEnum.valueOf(...)`. Renaming an
  enum constant also means updating those strings; IntelliJ's rename won't.
- The HTTP-call step supports only POST so far and throws for any other method; add GET/DELETE when a scenario
  needs them.
- The request specification is built once per run and cached, so every request logs to the same
  `requestLogs.txt`, which is overwritten at the start of each run.
- `getJsonPath` returns `null` for a missing JSON path (and logs a warning); a `null` in an assertion usually
  means a wrong path or an error response.
- `getPropertiesValue` returns `null` for a missing key (and logs a warning); check the key's spelling and case first.

## Decisions log

Add one line per decision so future sessions keep it.

- 2026-10-28: Framework is committed step by step, one `feature/*` branch and pull request per piece.
- 2026-10-29: Logging uses SLF4J + Logback; assertions log PASS/FAIL through `Assertions.assertEquals`.
- 2026-10-29: Methods renamed to Java camelCase (`logInformation`, `assertEquals`, `getPropertiesValue`, ...).
- 2026-10-30: Enum constants use UPPER_SNAKE_CASE without an `API` suffix (`ADD_PLACE`).
- 2026-10-30: Properties keys are dot-separated lowercase (`api.base.uri`, `api.key`).
- 2026-10-30: Packages keep the existing PascalCase names (`Pojo`, `Resources`, `Assertion`, `Logger`).
- 2026-10-30: Created this CLAUDE.md and the `best-practices` and `test-review` skills.
- 2026-10-31: POJO fields are camelCase, mapped to snake_case JSON keys with `@JsonProperty` (`phoneNumber` → `phone_number`).
- 2026-11-03: Feature files are PascalCase, one per API area (`PlaceApi.feature`), kept in `src/test/java/Features`.
- 2026-11-03: Shared request/response specs are `defaultRequestSpecification()` and `defaultResponseSpecification()`
  in `Utils`; the response spec checks JSON content only, and status codes are verified in the steps.
- 2026-11-03: Within a branch, work is committed in small working steps (feature file → stubs → steps → outline).
- 2026-11-05: Step definitions are exempt from the Javadoc rule unless their behaviour isn't obvious from the step text.