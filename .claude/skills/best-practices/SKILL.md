---
name: best-practices
description: Java + REST Assured + Cucumber API test automation standards for this project — project layout, naming, POJOs, configuration, endpoints, request and response specifications, response reading, assertions, logging, feature files, step definitions, hooks, scenario state (TestContext), API classes, reporting (Allure), Javadoc, formatting, dependencies and Git workflow. Use this skill whenever writing, generating, reviewing, refactoring, or debugging any POJO, utility, assertion, logger call, config value, endpoint, feature file, step definition, hook, TestContext, API class, TestRunner or report setup in this project, even if the request doesn't mention "best practices".
user-invocable: false
---

# REST API Test Automation Best Practices

These rules describe how this framework is built today. Follow them for every change, and update
this file in the same pull request when a rule changes.

## 1. Project layout

```
src/
├── main/java/
│   └── Pojo/                                   # Request/response models (serialized by Jackson)
├── test/java/
│   ├── Api/                                    # API layer
│   │   ├── ApiClient                           # sends step requests, registers created ids
│   │   └── PlaceApi                            # endpoint calls for hooks
│   ├── ApiDataBuilder/PlaceApiDataBuilder      # Test Data Builder: builds request payloads with defaults and overrides
│   ├── Assertion/Assertions                    # Logging assertions (PASS/FAIL lines)
│   ├── cucumber/Options/
│   │   └── TestRunner                          # JUnit runner: features path, glue, report plugins
│   ├── Context/TestContext                     # Per-scenario state shared by steps and hooks
│   ├── Features/                               # Cucumber feature files, one per API area (PlaceApi.feature)
│   ├── Logger/Log                              # The single shared logger
│   ├── Resources/
│   │   ├── Global.properties                   # Settings: base URI, API key
│   │   └── GlobalEnum                          # API endpoint paths
│   ├── Specification/Specifications            # Request/response specs
│   ├── StepDefinition/                         # CommonStepDefinition (shared steps), PlaceApiStepDefinition, Hooks
│   └── Utilities/                              # ConfigReader (settings), ResponseUtils (JSON path)
└── test/resources/
    ├── logback-test.xml                        # Log format and outputs
    └── allure.properties                       # Allure results folder (target/allure-results)
```

- Models that describe the API's JSON go in `src/main/java/Pojo`.
- Everything that only exists to run tests goes in `src/test/java`.
- Feature files stay in `src/test/java/Features`; the runner reads them by file path.
- Non-Java test files that must be on the classpath (like `logback-test.xml`) go in `src/test/resources`.
- Helpers (`Specifications`, `ConfigReader`, `ResponseUtils`) are final classes with a private constructor and only static methods.
  Use them through static imports; never extend them or create instances. One class per responsibility.

## 2. Naming

| Item                      | Convention                                                          | Example                                                               |
|---------------------------|---------------------------------------------------------------------|-----------------------------------------------------------------------|
| Class, enum               | PascalCase                                                          | `Assertions`, `GlobalEnum`                                            |
| Method                    | camelCase                                                           | `getPropertiesValue`, `defaultRequestSpecification`, `logInformation` |
| Step definition method    | camelCase, from the step text                                       | `addPlacePayloadWith`, `userCallsWithHttpRequest`                     |
| Variable, parameter       | camelCase                                                           | `baseUri`, `globalKey`                                                |
| Constant (`static final`) | UPPER_SNAKE_CASE                                                    | `PROPERTIES_FILE`, `REQUEST_LOG_FILE`                                 |
| Enum constant             | UPPER_SNAKE_CASE, no `API` suffix                                   | `ADD_PLACE`, `GET_PLACE`, `DELETE_PLACE`                              |
| Properties key            | lowercase, dot-separated                                            | `api.base.uri`, `api.key`                                             |
| Package                   | PascalCase (existing project style)                                 | `Pojo`, `Resources`, `Assertion`, `Logger`, `Api`, `Context`          |
| POJO field                | camelCase; map a different JSON key with `@JsonProperty`            | `phoneNumber` → `"phone_number"`                                      |
| Feature file              | PascalCase, one per API area                                        | `PlaceApi.feature`                                                    |
| Cucumber tag              | PascalCase with `@`                                                 | `@PlaceApi`, `@AddPlace`, `@Smoke`                                    |
| Branch                    | `<type>/<short-name>`; type `feature`, `refactor`, `fix` or `chore` | `feature/add-place-scenario`, `refactor/single-responsibility`        |

- Never name a method the same as a library method it calls (for example, the custom `assertEquals`
  calls JUnit as `Assert.assertEquals(...)`; otherwise it would call itself).
- Avoid naming a package after a class it uses (`Logger` vs SLF4J's `Logger`); prefer names that don't clash.

## 3. POJOs

- Private fields, a getter for each, and a **fluent setter** that returns `this`:

  ```java
  public AddPlace setName(String name) {
      this.name = name;
      return this;
  }
  ```

- Fields, getters and setters are camelCase. When the API's JSON key differs, map it on the field:

  ```java
  @JsonProperty("phone_number")
  private String phoneNumber;
  ```
- POJOs hold data only: no API calls, assertions or logging.

## 4. Configuration

- Settings live in `Resources/Global.properties` as `key = value`, without quotes.
- Read them only through `ConfigReader.getPropertiesValue("key")`. Never hard-code a base URI, API key or
  other setting in Java code.
- A missing key returns `null` and logs a warning, so check spelling and case first when a value is `null`.
- Real secrets (passwords, tokens, private keys) are never committed. The current `api.key` is the
  practice API's public demo key.

## 5. Endpoints

- Every endpoint path is a constant in `GlobalEnum`, read with `getResource()`.
- Paths start with `/` and are relative to `api.base.uri`.
- Add a new endpoint as a new constant; never hard-code a path elsewhere.
- An endpoint that creates something declares the JSON path of the new id as its second value (`ADD_PLACE("/…", "place_id")`).
  `ApiClient` uses it to register created ids for cleanup; an endpoint without it is never cleaned up.
- Feature files name endpoints by constant (`"ADD_PLACE"`); steps resolve them with `GlobalEnum.valueOf(...)`.
  Renaming a constant means updating those strings in the feature files too.

## 6. Requests and responses

- Every request starts from the shared spec: `given().spec(defaultRequestSpecification())`.
- The request spec is built once per run with the base URI, API key, JSON content type, and request/response
  logging to `requestLogs.txt` and the `AllureRestAssured` filter. Don't build a second spec or add these settings again per request.
- Every response is checked against the shared response spec with
  `.then().spec(defaultResponseSpecification()).extract().response()`. It only expects JSON content;
  status codes are verified in the steps, so each scenario can expect its own code and the check is logged.
- Request bodies are built in the API's data builder (`PlaceApiDataBuilder` for Place), one per API: POJOs for anything beyond a single field.
  Fixed test values (location, accuracy) are named constants there, never inline numbers.
- Setup that only needs a valid entity uses a default builder method (`defaultAddPlacePayload()`) with named
  fake constants: the Test Data Builder pattern.

## 7. Reading responses

- Read values with `getJsonPath(response, "path")`, which supports nested paths like `location.lat`.
- It returns `null` (and logs a warning) for a missing path, so a wrong path fails as a clear
  assertion instead of a `NullPointerException`.

## 8. Assertions

- Verify only through `Assertions`:
  - `assertEquals(description, expected, actual)` for any value.
  - `assertHttpStatus(response, code)` for an exact HTTP status; it also logs the response body when the status
    is wrong. Assert the exact code from the API contract, not just "any 2xx".
  - `assertHttpOk(response)` (200) and `assertHttpNotFound(response)` (404) are named shortcuts for it. Add a new
    shortcut only for a status that several steps check.
- All of them log `PASS | ...` or `FAIL | ...` with expected and actual values, then re-throw on failure.
- Don't call JUnit's `assertEquals` directly in tests; the result would not be logged.
- **Expected before actual**, always. Reversed, failures read the wrong way round.
- The description starts with `Verify` and names what is being checked:
  `"Verify status in response body"`, not just `"Verify"`.

## 9. Logging

- Use only `Logger.Log`: `logInformation`, `logWarning`, `logError`, via static import.
  Never use `System.out.println`.
- Log actions at INFO in plain language, including the values involved.
- Log a warning for something unexpected that doesn't fail the test (missing key, missing JSON path),
  and an error before re-throwing an exception.
- Don't log every successful read; the PASS lines already show the values that matter.
- Never log secrets (API keys, passwords, tokens).
- Output format: `HH:mm:ss | LEVEL | message` on the console, and one file per run in `target/logs/`.

## 10. Javadoc and comments

- Every class and every public method has Javadoc (`/** ... */`, generated with `/**` + Enter in IntelliJ).
- Exception: step definitions, whose annotation text already documents them. Give a step a short Javadoc
  only when its behaviour isn't obvious from the text: it reuses or is reused by another step, reads state
  from an earlier step, or makes extra API calls.
- The first sentence says what it does; add a second line only for non-obvious behaviour
  (caching, fixed values, what `null` means).
- `@param`, `@return` and `@throws` each have a description, not just the name.
- Inline `//` comments explain *why*, not *what*. Delete comments that only repeat the code
  (`// Variables`, `// Enums`) and Cucumber's generated `// Write code here...` comments.

## 11. Formatting

- Single-class imports (`import java.io.IOException;`), no wildcards. The only exception is a static
  import of this project's own helpers (`import static Logger.Log.*;`).
- Reformat Java files before committing: **Ctrl+Alt+L** (format) and **Ctrl+Alt+O** (optimize imports) in
  IntelliJ. Don't reformat Markdown files; it breaks code samples.
- One builder call per line for long chains (`RequestSpecBuilder`, fluent setters).
- No blank line directly after an opening `{`.
- Braces on every `if` / `else`, even for a single line.

## 12. Dependencies

- Declared in `pom.xml`; test-only libraries use `<scope>test</scope>`.
- Add or upgrade a dependency only in its own pull request, or as part of the feature that needs it,
  and say why in the description.
- Prefer a current version when adding a dependency; check mvnrepository first.

## 13. Test data

- The repository is public: never use real personal data (names, phone numbers, emails, addresses)
  anywhere in the code or feature files. Use clearly fake values such as `"Test Place 1"`, `9999999999`
  and `https://example.com`.

## 14. Generated files

Never commit build output, IDE files or local secrets. `.gitignore` covers:
`target/`, `.idea/`, `*.iml`, `requestLogs.txt`, `.allure/` and `src/test/java/Resources/secrets.properties` (local credentials).

## 15. Git workflow

- `main` is protected: every change goes through a `<type>/<name>` branch and a pull request reviewed by
  the code owner (`.github/CODEOWNERS`).
- One topic per branch and pull request; keep them small. Within a branch, commit in small working steps.
- Stage files explicitly (`git add <files>`); check `git status` before committing.
- Commit messages: imperative, describe the result — `Add API config, endpoint enum and request utilities`.
  Use a second `-m` for a bullet list of details when helpful.
- Pull request description sections: **What**, **Changes**, **How to test**.
- After merging: delete the branch on GitHub, then locally
  `git switch main`, `git pull`, `git branch -D <type>/<name>`.

## 16. Feature files and step definitions

### Feature files

```gherkin
@PlaceApi
Feature: Place API
  Add, get and delete places through the Place API.

  @AddPlace @Smoke
  Scenario Outline: Add a place and verify it can be fetched
    Given Add Place payload with "<Name>" "<Address>" "<Phone Number>" "<Language>" "<Website>" "<Types>"
    When User calls "ADD_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
    And the place created maps to "<Name>" using "GET_PLACE"

    Examples:
      | Name         | Address         | Phone Number | Language | Website                    | Types    |
      | Test Place 1 | 123 Main Street | 9876543210   | English  | https://example.com/place1 | Engineer |
```

- One feature file per API area, with a feature-level tag (`@PlaceApi`) and a one-line description.
- Scenario names say what is verified, as a short sentence that matches the steps actually in it.
- A scenario that changes data verifies the change with a follow-up read, not just the call's own response:
  after Add, GET finds the place; after Delete, GET no longer finds it.
- Every scenario has an area tag (`@AddPlace`) and a suite tag (`@Smoke` or `@Regression`).
- Steps are written in plain language. Values are passed as quoted strings or numbers, never as code
  (`"Engineer"`, not `"['Engineer']"`).

### Step definitions

- Step text in the annotation must match the feature text exactly; Cucumber links them by text, so change
  both together.
- New steps start as stubs that `throw new io.cucumber.java.PendingException();`, fully qualified so no import
  is added and removed again. Never leave a step body empty: an empty step passes and hides that nothing was tested.
- Use every step parameter. A parameter that the body ignores (or a value hard-coded instead of it)
  makes the feature lie about what is tested.
- Keep state between steps in `TestContext` (§17), never in fields of the step class.
- The HTTP-call step (in `CommonStepDefinition`) accepts any REST Assured `Method` (case-insensitive) and sends through `ApiClient`,
  which uses `request(Method, path)`. An unknown method logs an error and throws `IllegalArgumentException`, so a typo fails clearly.
- Steps that send a request call `ApiClient.send(...)`; a step class never calls another step class's step methods.
- Each step logs its action with `logInformation`, and verification steps log before they check.

## 17. Scenario state, hooks and the API layer

- Scenario state (request, response, ids) lives in `Context.TestContext`. cucumber-picocontainer creates one
  instance per scenario and injects it into every step and hook class that takes it in its constructor.
- Never share state through `static` fields, and never create glue classes with `new`: `new PlaceApiStepDefinition()`
  gets its own empty state, not the scenario's.
- Hooks live in `StepDefinition/Hooks`. Setup hooks are scoped by tag (`@Before("@DeletePlace")`), because
  different scenarios need different data. Use them for setup the reader doesn't need to see; business setup the
  scenario is about stays a visible step.
- Teardown is driven by resource tracking, not by tags: whoever creates a resource registers it at once (before any check that could fail):
  `ApiClient` for calls from steps, via the endpoint's id path, and setup hooks with `testContext.addCreatedId(endpoint, id)`.
  The untagged `@After` cleanup hook deletes every tracked resource after every scenario, passed or failed.
- Cleanup never fails a scenario. 200 (deleted) and 404 (already gone) are logged as info; any other status or
  exception is logged as a warning with the status and body.
- Hooks depend on `TestContext`, `Api` classes and `PlaceApiDataBuilder`, never on step classes. When a hook creates
  data a step uses, add a `#` comment in the scenario that names the hook.
- A setup hook verifies its own call (`assertHttpOk`), so a failed setup fails in the hook and skips the steps.
- `Api` classes (`PlaceApi`) send one request each (`addPlace`, `deletePlace`) from the shared specs and return
  the `Response`: no state, no assertions. Endpoints come from `GlobalEnum` constants (`GlobalEnum.ADD_PLACE`),
  not strings.
- `ApiClient` is injected (PicoContainer), not static, because it uses the scenario's `TestContext`.
  Shared classes (`ApiClient`, `CommonStepDefinition`) contain no resource-specific conditions.

## 18. Reporting

- `TestRunner` writes two reports: Allure results to `target/allure-results` (open with Maven → Plugins → allure →
  `allure:serve`) and a single-file Cucumber report to `target/cucumber-reports/cucumber.html`.
- Reports are written only when `TestRunner` (or `mvn test`) runs. The green arrow in a `.feature` file uses
  IntelliJ's own Cucumber configuration and ignores `@CucumberOptions`.
- Requests and responses reach the Allure report through the `AllureRestAssured` filter on the shared request spec.
  Never add it per request; every call already gets it from the shared spec.
- Never put real secrets in requests that are attached to the report: mask them first. The current `api.key` is
  the public demo key.
- Allure modules are versioned through `allure-bom` (one `allure.version` property). The project stays on the
  Allure Java 2.x line until the pom targets Java 17, which Allure 3 requires.

## Checklist before opening a pull request

- [ ] Compiles: Maven → Lifecycle → test-compile shows BUILD SUCCESS
- [ ] Tests run: TestRunner passes (the build reports SUCCESS even when tests fail, so check the results)
- [ ] No hard-coded settings, endpoints or secrets in Java code
- [ ] Assertions go through `Assertions` with a specific `Verify ...` description, expected first
- [ ] Feature steps and step definitions match exactly; no empty step bodies; every parameter used
- [ ] Logging through `Log`, no `System.out`
- [ ] Javadoc on every new class and public method (step definitions only when their behaviour isn't obvious)
- [ ] Imports optimized, Java code formatted
- [ ] No real personal data in code or feature files
- [ ] Only this topic's files staged
- [ ] Shared state through `TestContext` only: no `static` state, no `new PlaceApiStepDefinition()`
- [ ] A new or changed rule is applied to existing code too: search for the old pattern and fix every match
- [ ] After adding a tool or plugin, run it once and check `git status`: generated files go in `.gitignore`
