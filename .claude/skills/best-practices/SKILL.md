---
name: best-practices
description: Java + REST Assured + Cucumber API test automation standards for this project — project layout, naming, POJOs, configuration, endpoints, request and response specifications, response reading, assertions, logging, feature files, step definitions, Javadoc, formatting, dependencies and Git workflow. Use this skill whenever writing, generating, reviewing, refactoring, or debugging any POJO, utility, assertion, logger call, config value, endpoint, feature file or step definition in this project, even if the request doesn't mention "best practices".
user-invocable: false
---

# REST API Test Automation Best Practices

These rules describe how this framework is built today. Follow them for every change, and update
this file in the same pull request when a rule changes.

## 1. Project layout

```
src/
├── main/java/
│   └── Pojo/                     # Request/response models (serialized by Jackson)
├── test/java/
│   ├── Assertion/Assertions      # Logging assertions (PASS/FAIL lines)
│   ├── Logger/Log                # The single shared logger
│   ├── Resources/
│   │   ├── Global.properties     # Settings: base URI, API key
│   │   ├── GlobalEnum            # API endpoint paths
│   │   ├── TestDataBuilder       # Builds request payloads
│   │   └── Utils                 # Request/response specs, properties reader, JSON helper
│   ├── Features/                 # Cucumber feature files, one per API area (PlaceApi.feature)
│   ├── StepDefinition/           # Step definitions for the feature files
│   └── cucumber/Options/
│       └── TestRunner            # JUnit runner: features path, glue, report plugins
└── test/resources/
    └── logback-test.xml          # Log format and outputs
```

- Models that describe the API's JSON go in `src/main/java/Pojo`.
- Everything that only exists to run tests goes in `src/test/java`.
- Feature files stay in `src/test/java/Features`; the runner reads them by file path.
- Non-Java test files that must be on the classpath (like `logback-test.xml`) go in `src/test/resources`.

## 2. Naming

| Item                      | Convention                                               | Example                                                               |
|---------------------------|----------------------------------------------------------|-----------------------------------------------------------------------|
| Class, enum               | PascalCase                                               | `Assertions`, `GlobalEnum`                                            |
| Method                    | camelCase                                                | `getPropertiesValue`, `defaultRequestSpecification`, `logInformation` |
| Step definition method    | camelCase, from the step text                            | `addPlacePayloadWith`, `userCallsWithHttpRequest`                     |
| Variable, parameter       | camelCase                                                | `baseUri`, `globalKey`                                                |
| Constant (`static final`) | UPPER_SNAKE_CASE                                         | `PROPERTIES_FILE`, `REQUEST_LOG_FILE`                                 |
| Enum constant             | UPPER_SNAKE_CASE, no `API` suffix                        | `ADD_PLACE`, `GET_PLACE`, `DELETE_PLACE`                              |
| Properties key            | lowercase, dot-separated                                 | `api.base.uri`, `api.key`                                             |
| Package                   | PascalCase (existing project style)                      | `Pojo`, `Resources`, `Assertion`, `Logger`                            |
| POJO field                | camelCase; map a different JSON key with `@JsonProperty` | `phoneNumber` → `"phone_number"`                                      |
| Feature file              | PascalCase, one per API area                             | `PlaceApi.feature`                                                    |
| Cucumber tag              | PascalCase with `@`                                      | `@PlaceApi`, `@AddPlace`, `@Smoke`                                    |
| Branch                    | `feature/<short-name>`                                   | `feature/add-place-scenario`                                          |

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
- Read them only through `Utils.getPropertiesValue("key")`. Never hard-code a base URI, API key or
  other setting in Java code.
- A missing key returns `null` and logs a warning, so check spelling and case first when a value is `null`.
- Real secrets (passwords, tokens, private keys) are never committed. The current `api.key` is the
  practice API's public demo key.

## 5. Endpoints

- Every endpoint path is a constant in `GlobalEnum`, read with `getResource()`.
- Paths start with `/` and are relative to `api.base.uri`.
- Add a new endpoint as a new constant; never hard-code a path elsewhere.
- Feature files name endpoints by constant (`"ADD_PLACE"`); steps resolve them with `GlobalEnum.valueOf(...)`.
  Renaming a constant means updating those strings in the feature files too.

## 6. Requests and responses

- Every request starts from the shared spec: `given().spec(defaultRequestSpecification())`.
- The request spec is built once per run with the base URI, API key, JSON content type, and request/response
  logging to `requestLogs.txt`. Don't build a second spec or add these settings again per request.
- Every response is checked against the shared response spec with
  `.then().spec(defaultResponseSpecification()).extract().response()`. It only expects JSON content;
  status codes are verified in the steps, so each scenario can expect its own code and the check is logged.
- Request bodies are built in `TestDataBuilder`: POJOs for anything beyond a single field.
  Fixed test values (location, accuracy) are named constants there, never inline numbers.

## 7. Reading responses

- Read values with `getJsonPath(response, "path")`, which supports nested paths like `location.lat`.
- It returns `null` (and logs a warning) for a missing path, so a wrong path fails as a clear
  assertion instead of a `NullPointerException`.

## 8. Assertions

- Verify only through `Assertions`:
  - `assertEquals(description, expected, actual)` for any value.
  - `assertHttpOk(response)` for an HTTP 200 status; it also logs the response body when the status is wrong.
- Both log `PASS | ...` or `FAIL | ...` with expected and actual values, then re-throw on failure.
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

Never commit build output or IDE files. `.gitignore` covers:
`target/`, `.idea/`, `*.iml`, `requestLogs.txt`.

## 15. Git workflow

- `main` is protected: every change goes through a `feature/*` branch and a pull request reviewed by
  the code owner (`.github/CODEOWNERS`).
- One topic per branch and pull request; keep them small. Within a branch, commit in small working steps.
- Stage files explicitly (`git add <files>`); check `git status` before committing.
- Commit messages: imperative, describe the result — `Add API config, endpoint enum and request utilities`.
  Use a second `-m` for a bullet list of details when helpful.
- Pull request description sections: **What**, **Changes**, **How to test**.
- After merging: delete the branch on GitHub, then locally
  `git switch main`, `git pull`, `git branch -D feature/<name>`.

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
- Every scenario has an area tag (`@AddPlace`) and a suite tag (`@Smoke` or `@Regression`).
- Steps are written in plain language. Values are passed as quoted strings or numbers, never as code
  (`"Engineer"`, not `"['Engineer']"`).

### Step definitions

- Step text in the annotation must match the feature text exactly; Cucumber links them by text, so change
  both together.
- New steps start as generated stubs that `throw new PendingException()`. Never leave a step body empty:
  an empty step passes and hides that nothing was tested.
- Use every step parameter. A parameter that the body ignores (or a value hard-coded instead of it)
  makes the feature lie about what is tested.
- Keep state between steps in private fields (`requestSpecification`, `response`).
- The HTTP-call step supports only the methods it implements and throws `IllegalArgumentException` for any
  other, so a typo fails clearly instead of leaving `response` as `null`.
- Each step logs its action with `logInformation`, and verification steps log before they check.

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
