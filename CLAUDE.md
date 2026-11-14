# CLAUDE.md — RestApiFramework

REST API test automation framework built with Java, REST Assured, Cucumber and Maven, for the Rahul Shetty
Academy Place API and e-commerce (Product) API. Study project, committed step by step: each piece arrives through its own `feature/*`, `refactor/*`, `fix/*` or `chore/*` branch and pull request.

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
- Run from the project root: `ConfigReader` locates `Global.properties`, and `TestRunner` locates the feature files,
  relative to the folder Maven is started from.

## Commands

- Compile everything, including test code: `mvn test-compile` (should end with BUILD SUCCESS)
- Run all scenarios: `mvn test`, or run `TestRunner` in IntelliJ (green arrow next to the class)
- Run by tag: `mvn test -Dcucumber.filter.tags="@AddPlace"`
- Run one scenario: green arrow next to it in the `.feature` file
- Open the Allure report: Maven tool window → Plugins → allure → `allure:serve` (after running `TestRunner`)
- Quick report without Maven: open `target/cucumber-reports/cucumber.html` in a browser

## Layout

- `src/main/java/Pojo/` request models: `AddPlace`, `Location`, `Login` (JSON, via Jackson), `Product` (multipart form fields)
- `src/test/java/Api/PlaceApi` endpoint calls for hooks and setup (stateless, no assertions)
- `src/test/java/Api/ApiClient` sends the scenario's prepared request, stores the response, registers created ids for cleanup
- `src/test/java/Api/LoginApi` logs in to the e-commerce API once per run and caches the session
- `src/test/java/Api/ProductApi` Product requests (create, get, delete) with the session's token
- `src/test/java/ApiDataBuilder/PlaceApiDataBuilder` Test Data Builder: builds request payloads with defaults and overrides
- `src/test/java/ApiDataBuilder/LoginApiDataBuilder`, `ProductApiDataBuilder` login and product payloads, one builder per API
- `src/test/java/Assertion/Assertions` logging assertions: `assertEquals(description, expected, actual)`,
  `assertHttpStatus(response, code)` and its shortcuts `assertHttpOk` (200) and `assertHttpNotFound` (404)
- `src/test/java/Context/TestContext` per-scenario state, injected by cucumber-picocontainer
- `src/test/java/Context/AuthSession` immutable token and user id of the run's e-commerce session
- `src/test/java/cucumber/Options/TestRunner` JUnit runner: features path, glue, `pretty`, Cucumber HTML and Allure plugins
- `src/test/java/Features/` Cucumber feature files: `PlaceApi.feature`, `ProductApi.feature`
- `src/test/java/Logger/Log` shared logger: `logInformation`, `logWarning`, `logError`
- `src/test/java/Resources/` `Global.properties` (settings), `GlobalEnum` (endpoints; create endpoints also declare their id path),
  `secrets.properties` (credentials, git-ignored; copy `secrets.properties.example`)
- `src/test/java/Specification/Specifications` request specs `defaultRequestSpecification()` (Place),
  `loginRequestSpecification()` (unlogged) and `ecomRequestSpecification()` (token masked), plus the response spec
  `defaultResponseSpecification()`
- `src/test/java/StepDefinition/CommonStepDefinition` steps shared by every feature (http call, status, response body)
- `src/test/java/StepDefinition/LoginStepDefinition` the "the user is logged in" step
- `src/test/java/StepDefinition/ProductApiStepDefinition` Product API steps
- `src/test/java/StepDefinition/ProductHooks` `@Before("@DeleteProduct")` creates the product to delete; `@After` deletes
  every product the scenario created
- `src/test/java/StepDefinition/PlaceApiStepDefinition` Place API steps
- `src/test/java/StepDefinition/Hooks` tag-scoped setup (`@Before("@DeletePlace")` creates the place to delete) and
  an `@After` hook that deletes every place the scenario created
- `src/test/java/Utilities/` `ConfigReader` (settings and secrets), `ResponseUtils` (JSON helper)
- `src/test/resources/allure.properties` Allure results folder (`target/allure-results`)
- `src/test/resources/logback-test.xml` log format and outputs
- `src/test/resources/files/product.jpeg` placeholder image uploaded by Create Product
- Generated, never edit or commit: `target/`, `requestLogs.txt`, `.allure/` (Allure tool downloaded by `allure:serve`)

## Conventions (summary; the skill has the full detail)

- Methods and variables camelCase; constants and enum constants UPPER_SNAKE_CASE; properties keys
  dot-separated lowercase (`api.base.uri`); packages keep the existing PascalCase style.
- Settings only in `Global.properties`, read with `ConfigReader.getPropertiesValue`; endpoints only in `GlobalEnum`.
- Every request starts from `given().spec(defaultRequestSpecification())`; every response is checked with
  `.then().spec(defaultResponseSpecification())` (JSON content only; status codes are checked in the steps).
- Verify only through `Assertions` (`assertEquals`, `assertHttpStatus` and its shortcuts), expected before actual,
  with a specific `Verify ...` description.
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
- The HTTP-call step (in `CommonStepDefinition`) accepts any REST Assured `Method` (case-insensitive) and throws
  `IllegalArgumentException` for anything else.
- A failing `@Before` hook marks the scenario failed and skips all its steps; check the hook's log lines first.
- Step and hook classes get `TestContext` through their constructor; creating one with `new` gives a separate,
  empty context, and later steps see `null`.
- Feature files name endpoints as strings (`"ADD_PLACE"`) resolved with `GlobalEnum.valueOf(...)`. Renaming an
  enum constant also means updating those strings; IntelliJ's rename won't.
- The request specification is built once per run and cached, so every request logs to the same
  `requestLogs.txt`, which is overwritten at the start of each run.
- `getJsonPath` returns `null` for a missing JSON path (and logs a warning); a `null` in an assertion usually
  means a wrong path or an error response.
- `getPropertiesValue` returns `null` for a missing key (and logs a warning); check the key's spelling and case first.
- Created resources are cleaned up only if they are registered. `ApiClient` registers them automatically when the
  endpoint declares its id path in `GlobalEnum` (`ADD_PLACE(…, "place_id")`); a new create endpoint without an id
  path is never cleaned up. Setup hooks that call an `Api` class directly register with `testContext.addCreatedId(...)`.
- Cleanup log lines (`Cleanup: …`) appear after the last step; a `WARN | Cleanup: could not delete place` line
  means a place may have been left on the live API.
- Reports are written only when `TestRunner` (or `mvn test`) runs; the green arrow in a `.feature` file ignores
  `@CucumberOptions`, so no Allure or HTML report is produced.
- `allure:serve` serves the report on the machine's network IP while it runs; stop it when done.
- `@Before` hooks run before the `Background`: a setup hook that needs the e-commerce session gets it from
  `LoginApi.session()` itself, because the "the user is logged in" step has not run yet.
- The e-commerce API returns 201 for Create Product, and 400 with "Product not found" (not 404) for a missing product;
  the delete endpoint spells it "Product not foundd". Steps check status and message; cleanup uses `startsWith`.
- Masking the Authorization header needs two settings: the `RequestLoggingFilter` blacklist (requestLogs.txt) and the
  `LogConfig` blacklist (Allure). Removing either one leaks the token into that output.
- A missing `secrets.properties`, or a blank key in it, stops the run with a clear error; copy
  `secrets.properties.example` and fill in a test account.

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
- 2026-11-06: TestDataBuilder follows the Test Data Builder pattern: it owns payload defaults (`defaultAddPlacePayload`)
  as named fake constants; values move to JSON files only when scenarios need many variants.
- 2026-11-06: The HTTP-call step sends any method with RestAssured `request(Method, path)` instead of one branch per method.
- 2026-11-06: Scenario state lives in `Context.TestContext`, injected by cucumber-picocontainer into step and hook classes; a new instance per scenario.
- 2026-11-06: Setup calls outside the step flow go through `Api.PlaceApi`; the `@DeletePlace` hook creates the place each Delete scenario deletes.
- 2026-11-07: Status checks go through `Assertions.assertHttpStatus(response, code)` and assert the exact code from
  the API contract; `assertHttpOk` (200) and `assertHttpNotFound` (404) are named shortcuts.
- 2026-11-07: A scenario that changes data verifies the change with a follow-up GET (Add: the place is found;
  Delete: it is not found).
- 2026-11-07: Teardown uses resource tracking: places are registered in `TestContext` when created, and an untagged
  `@After` hook deletes them after every scenario. Cleanup never fails a scenario (200/404 info, anything else warns).
- 2026-11-07: Reporting uses Allure (2.x line, via `allure-bom`) with the `AllureRestAssured` filter on the shared
  request spec, plus Cucumber's single-file HTML report; the masterthought plugin and the JSON output are removed.
- 2026-11-09: Single-responsibility refactor: Utils split into `Specification/Specifications` (request/response specs),
  `Utilities/ConfigReader` (settings) and `Utilities/ResponseUtils` (JSON reading); helpers are final classes with static
  methods, used via static imports, never extended; TestDataBuilder → `ApiDataBuilder/PlaceApiDataBuilder` and
  StepDefinition → `PlaceApiStepDefinition`, one data builder and one step class per API.
- 2026-11-09: Branches are named `<type>/<short-name>` with type `feature`, `refactor`, `fix` or `chore`; one topic per branch.
- 2026-11-10: Create endpoints declare the JSON path of the new id in `GlobalEnum`; `ApiClient` registers created ids per endpoint in `TestContext` for cleanup,
  so shared code has no resource-specific conditions and features stay unchanged.
- 2026-11-10: Generic steps live in `CommonStepDefinition`; requests from steps go through `ApiClient`; step classes never call each other's step methods.
- 2026-11-14: E-commerce credentials live in the git-ignored `src/test/java/Resources/secrets.properties` (with a committed
  `.example`), read only through `ConfigReader.getSecretValue`; never in `Global.properties`, code or logs.
- 2026-11-14: `LoginApi` logs in once per run and caches an immutable `AuthSession` in a static field; a deliberate,
  run-scoped exception to "no static state". Scenario state stays in `TestContext`. Its login response skips the
  shared response spec on purpose, so a failed login reports its status and which secrets to check instead of a
  content-type error.
- 2026-11-14: Three request specs: default (Place), login (no logging, it carries credentials) and e-commerce (logged,
  with the Authorization header masked by both a `RequestLoggingFilter` blacklist and a `LogConfig` blacklist).
- 2026-11-14: One hook class per API (`ProductHooks`); facts about an API's contract (e.g. `PRODUCT_NOT_FOUND_MESSAGE`)
  live in its Api class; status codes use the JDK's `HttpURLConnection` constants.
- 2026-11-14: Tests assert the API's actual contract (Create Product 201; missing product 400 + "Product not found")
  and document deviations from REST conventions in Gotchas.