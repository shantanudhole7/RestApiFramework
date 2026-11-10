# RestApiFramework – REST Assured API Test Automation Framework

API test automation framework for the [Rahul Shetty Academy](https://rahulshettyacademy.com) **Place API**,
built with **Java**, **REST Assured**, **Cucumber** and **Maven**. Scenarios are written in Gherkin, requests are
built from POJOs and a shared request specification, and every check is logged as PASS or FAIL.

---

## Tech Stack

| Tool             | Purpose                                                          |
|------------------|------------------------------------------------------------------|
| Java 11+         | Language                                                         |
| Maven            | Build and dependencies                                           |
| REST Assured     | HTTP requests, request specification and request logging         |
| Jackson          | Serializes POJOs to JSON request bodies                          |
| Cucumber + JUnit | BDD scenarios in Gherkin, step definitions, test runner          |
| PicoContainer    | Injects one shared TestContext per scenario into steps and hooks |
| Allure           | Test report with every request and response attached per step    |
| SLF4J + Logback  | Framework logging: console and one log file per run              |

---

## Project Structure

```
RestApiFramework/
├── .claude/skills/
│   ├── best-practices/SKILL.md                         # Coding standards (applied by Claude Code automatically)
│   └── test-review/SKILL.md                            # /test-review: reviews the code against the standards
├── .github/CODEOWNERS                                  # Every pull request needs the code owner's review
├── src/
│   ├── main/java/
│   │   └── Pojo/                                       # Request models: AddPlace, Location
│   └── test/
│       ├── java/
│       │   ├── Api/
│       │   │   ├── ApiClient.java                      # Sends requests from steps; registers created ids for cleanup
│       │   │   └── PlaceApi.java                       # Endpoint calls for hooks and setup; returns the response
│       │   ├── ApiDataBuilder/
│       │   │   └── PlaceApiDataBuilder.java            # Builds Add Place and Delete Place payloads
│       │   ├── Assertion/                              # Assertions: logging assertEquals and HTTP status checks (PASS/FAIL)
│       │   ├── Context/
│       │   │   └── TestContext.java                    # Per-scenario state: request, response, place_id
│       │   ├── cucumber/Options/
│       │   │   └── TestRunner.java                     # JUnit runner: features path, glue, report plugins
│       │   ├── Features/
│       │   │   └── PlaceApi.feature                    # Place API scenarios
│       │   ├── Logger/                                 # Log: logInformation, logWarning, logError
│       │   ├── Resources/
│       │   │   ├── Global.properties                   # Settings: base URI, API key
│       │   │   └── GlobalEnum.java                     # Endpoint paths: ADD_PLACE, GET_PLACE, DELETE_PLACE
│       │   ├── Specification/
│       │   │   └── Specifications.java                 # Request/response specs
│       │   ├── StepDefinition/
│       │   │   ├── CommonStepDefinition.java           # Steps shared by every feature
│       │   │   ├── Hooks.java                          # Setup for @DeletePlace, and cleanup of created places
│       │   │   └── PlaceApiStepDefinition.java         # Place API steps, plus the generic steps shared by every feature
│       │   └── Utilities/
│       │       ├── ConfigReader.java                   # Reads configuration properties
│       │       └── ResponseUtils.java                  # Utility methods for handling API responses
│       └── resources/
│           ├── logback-test.xml                        # Log format and outputs
│           └── allure.properties                       # Allure results folder
├── CLAUDE.md                                           # Project context and rules for Claude Code
├── pom.xml                                             # Dependencies and plugins
└── README.md
```

Generated and never committed: `target/` (build output, logs and reports), `requestLogs.txt` and `.allure/` (the
Allure tool downloaded by `allure:serve`).

---

## Setup

**1. Prerequisites:** JDK 11 or newer, and Maven, either installed on the PATH or the one built into IntelliJ IDEA.

**2. Clone and open**

```bash
git clone https://github.com/shantanudhole7/RestApiFramework.git
```

Open the folder in IntelliJ IDEA as a Maven project and let it download the dependencies.

**3. Check `src/test/java/Resources/Global.properties`**

```properties
# Base URI of the Place API; endpoint paths come from GlobalEnum
api.base.uri=https://rahulshettyacademy.com

# Public demo key for the practice API, sent as the "key" query parameter
api.key=qaclick123
```

Write values without quotes. Keys are case-sensitive.

**4. Build**

```bash
mvn test-compile
```

Without Maven on the PATH, use IntelliJ's **Maven tool window → Lifecycle → test-compile**. It should end with **BUILD
SUCCESS**.

---

## Running Tests

Run from the project root. The scenarios call the live practice API.

| Goal                  | Command                                                       |
|-----------------------|---------------------------------------------------------------|
| All scenarios         | `mvn test`, or run `TestRunner` in IntelliJ                   |
| By tag                | `mvn test -Dcucumber.filter.tags="@AddPlace"`                 |
| One scenario          | Green arrow next to the scenario in the `.feature` file       |

Tags: `@PlaceApi` (every Place API scenario), `@AddPlace`, `@DeletePlace`, `@Smoke`.

**Note:** `pom.xml` sets `testFailureIgnore = true`, so Maven reports **BUILD SUCCESS even when scenarios fail**.
Always check the test results in the console (`pretty` output) or the report.

### Reports

Run `TestRunner` (or `mvn test`), then:

| Report        | How to open                                               | What it shows                                                                                      |
|---------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Allure        | Maven tool window → Plugins → allure → `allure:serve`     | Scenarios, steps, hooks, timings, and every request and response attached to the step that sent it |
| Cucumber HTML | Open `target/cucumber-reports/cucumber.html` in a browser | Quick single-file overview, filterable by tag                                                      |

Running a scenario from the green arrow in the `.feature` file doesn't write reports.

---

## How the Framework Works

### Configuration and endpoints

Settings live in `Global.properties` and are read only through `ConfigReader.getPropertiesValue("key")`. A missing key
returns `null` and logs a warning.

Endpoint paths live in `GlobalEnum`, relative to the base URI:

```java
ADD_PLACE("/maps/api/place/add/json", "place_id"),
GET_PLACE("/maps/api/place/get/json"),
DELETE_PLACE("/maps/api/place/delete/json");
```
`ADD_PLACE` also declares where its response holds the new id, so its places are cleaned up automatically.

### Request specification

Every request starts from one shared specification, built once per run by `Specifications.defaultRequestSpecification()`:

- base URI and API key from `Global.properties`
- JSON content type
- every request and response logged to `requestLogs.txt`
- every request and response attached to the Allure report (`AllureRestAssured` filter)

```java
given().spec(defaultRequestSpecification()).body(payload)
```

Every response is checked against the shared `Specifications.defaultResponseSpecification()`, which expects JSON content.
Status codes are verified in the steps, so each scenario can expect its own code and the check is logged.
Steps send every request through `ApiClient.send(endpoint, method)`, which does this:

```java
Response response = testContext.getRequestSpecification()
                               .when()
                               .request(httpMethod, resource)
                               .then()
                               .spec(defaultResponseSpecification())
                               .extract()
                               .response();
```

The HTTP method comes from the feature text (`"POST"`, `"GET"`, case-insensitive) and is sent with REST Assured's
`request(Method, path)`, so every method works without a branch per method. An unknown method fails with
`Unsupported HTTP method`.

### Request bodies

`PlaceApiDataBuilder` builds the request bodies:

- `addPlacePayload(name, address, phoneNumber, language, website, types)` returns an `AddPlace` POJO, serialized to
  JSON by Jackson. The location (`LATITUDE`, `LONGITUDE`) and `ACCURACY` are fixed test values, defined as constants.
- `deletePlacePayload(placeId)` returns the JSON body `{"place_id": "<placeId>"}`.
- `defaultAddPlacePayload()` returns an `AddPlace` with fixed fake values, for setup that only needs a place to
  exist. This is the Test Data Builder pattern: defaults in one place, tests override only what they care about.

```java
PlaceApiDataBuilder placeApiDataBuilder = new PlaceApiDataBuilder();
AddPlace addPlace = placeApiDataBuilder.addPlacePayload(name, address, phoneNumber, language, website, types);
String deleteBody = placeApiDataBuilder.deletePlacePayload(placeId);
AddPlace setupPlace = placeApiDataBuilder.defaultAddPlacePayload();
```

POJO fields are camelCase; JSON keys that differ are mapped with `@JsonProperty`, such as
`phoneNumber` → `"phone_number"`.

### Reading responses

`ResponseUtils.getJsonPath(response, "path")` reads a value from a JSON response, including nested paths such as
`location.lat`. A missing path returns `null` and logs a warning, so an assertion fails clearly.

### Assertions and logging

Every check goes through `Assertions.assertEquals(description, expected, actual)`, which logs a PASS or FAIL line
with both values and re-throws on failure:

```
19:40:56 | INFO  | PASS | Verify status in response body | Expected: OK | Actual: OK
```

`Assertions.assertHttpStatus(response, code)` checks for an exact HTTP status the same way, and also logs the
response body when the status is wrong, since it usually explains the error. `assertHttpOk(response)` (200) and
`assertHttpNotFound(response)` (404) are named shortcuts for it. Steps assert the exact code from the API contract,
not just "any 2xx".

Other messages go through `Log.logInformation`, `logWarning` and `logError`. Logs are written to the console and
to one file per run in `target/logs/test_run_<timestamp>.log`, as configured in `logback-test.xml`.

### Scenario state, hooks and the API layer

Cucumber creates new step and hook objects for every scenario, so scenarios never share state. Within one
scenario, steps and hooks share a `TestContext` (request, response, `place_id`). `cucumber-picocontainer` creates
one per scenario and passes the same instance to every class that takes it in its constructor:

```java
public PlaceApiStepDefinition(TestContext testContext, ApiClient apiClient) {
    this.testContext = testContext;
    this.apiClient = apiClient;
}
```

Hooks in `StepDefinition/Hooks.java` run setup selected by tag. Before every `@DeletePlace` scenario, a hook
creates a place and stores its `place_id`, so each Delete scenario deletes a place of its own and can run alone,
in any order:

```java
@Before("@DeletePlace")
public void createPlaceForDelete() throws IOException {
    Response response = placeApi.addPlace(placeApiDataBuilder.defaultAddPlacePayload());
    String placeId = getJsonPath(response, GlobalEnum.ADD_PLACE.getCreatedIdPath());
    testContext.addCreatedId(GlobalEnum.ADD_PLACE, placeId);   // tracked before the check, so cleanup still deletes it
    assertHttpOk(response);
    testContext.setPlaceId(placeId);
}
```

Hooks call the API through `Api/PlaceApi`, not through step methods. `PlaceApi` sends one request from the shared
specs and returns the `Response`; it keeps no state and makes no assertions, so the caller decides what to check.

After every scenario, passed or failed, an untagged `@After` hook deletes every place the scenario created. Places
are registered in `TestContext` the moment they are created (by `ApiClient` after any call to an endpoint that declares an id path,
and by the setup hook), so a scenario that fails halfway still cleans up. Cleanup never fails a scenario: a place that is
already gone is logged as info, and any other problem as a warning.

```java
@After
public void cleanUpCreatedPlaces() {
    for (String placeId : testContext.getCreatedIds(GlobalEnum.ADD_PLACE)) {
        Response response = placeApi.deletePlace(placeId);   // 200 deleted, 404 already gone, else warning
    }
}
```

### Scenarios and step definitions

Scenarios live in `src/test/java/Features/`, one feature file per API area:

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

  @DeletePlace @Smoke
  Scenario: Delete a place and verify the delete succeeds
    # The @DeletePlace hook creates the place to delete (Hooks.createPlaceForDelete)
    Given Delete Place payload for the created place
    When User calls "DELETE_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
    And "status" in response body is "OK"
    And the deleted place can no longer be fetched using "GET_PLACE"
```

Each step maps to a method in `PlaceApiStepDefinition` by its exact text or, for the generic steps (`User calls…`, `status code`, `response body`), `CommonStepDefinition`:

- **Given** builds the request from the shared spec and a `PlaceApiDataBuilder` payload.
- **When** resolves the endpoint name through `GlobalEnum` and sends the request through `ApiClient`.
- **Then** verifies the response with `Assertions`.
- **`the place created maps to "<Name>" using "GET_PLACE"`** reads `place_id` from the response, calls `GET_PLACE`
  with it, and checks the name.
- **`"<key>" in response body is "<value>"`** reads any JSON key from the last response and checks its value.
- **`the deleted place can no longer be fetched using "GET_PLACE"`** calls `GET_PLACE` with the deleted `place_id`
  and expects 404 Not Found.

`TestRunner` runs every feature file with JUnit, prints each step (`pretty`), and writes the Allure results and the
Cucumber HTML report (see [Reports](#reports)).

---

## Contributing

`main` is protected. Every change goes through a pull request reviewed by the code owner.

```bash
git switch main
git pull
git switch -c <type>/<name>
git push -u origin <type>/<name>
# make changes, in small working steps
git add <files>
git commit -m "Add <what changed>"
git push
```

Then open a pull request into `main` with a **What**, **Changes** and **How to test** section.
Branch names start with `feature/`, `refactor/`, `fix/` or `chore/`.

---

## Claude Code Skills

- **best-practices:** the project's coding standards (layout, naming, POJOs, config, endpoints, requests,
  assertions, logging, Javadoc, scenario state, hooks and the API layer, reporting, Git workflow). Claude Code applies it
  automatically when writing or changing code.
- **test-review:** reviews feature files, step definitions and framework code against those standards, including
  that every step matches its definition and uses its parameters. Run `/test-review` for everything, or
  `/test-review <file>` for one file.