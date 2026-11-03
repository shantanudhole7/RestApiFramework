# RestApiFramework – REST Assured API Test Automation Framework

API test automation framework for the [Rahul Shetty Academy](https://rahulshettyacademy.com) **Place API**,
built with **Java**, **REST Assured**, **Cucumber** and **Maven**. Scenarios are written in Gherkin, requests are
built from POJOs and a shared request specification, and every check is logged as PASS or FAIL.

---

## Tech Stack

| Tool             | Purpose                                                  |
|------------------|----------------------------------------------------------|
| Java 11+         | Language                                                 |
| Maven            | Build and dependencies                                   |
| REST Assured     | HTTP requests, request specification and request logging |
| Jackson          | Serializes POJOs to JSON request bodies                  |
| Cucumber + JUnit | BDD scenarios in Gherkin, step definitions, test runner  |
| SLF4J + Logback  | Framework logging: console and one log file per run      |

---

## Project Structure

```
RestApiFramework/
├── .claude/skills/
│   ├── best-practices/SKILL.md               # Coding standards (applied by Claude Code automatically)
│   └── test-review/SKILL.md                  # /test-review: reviews the code against the standards
├── .github/CODEOWNERS                        # Every pull request needs the code owner's review
├── src/
│   ├── main/java/
│   │   └── Pojo/                             # Request models: AddPlace, Location
│   └── test/
│       ├── java/
│       │   ├── Assertion/                    # Assertions: logging assertEquals (PASS/FAIL lines)
│       │   ├── Logger/                       # Log: logInformation, logWarning, logError
│       │   ├── Resources/
│       │   │   ├── Global.properties         # Settings: base URI, API key
│       │   │   ├── GlobalEnum.java           # Endpoint paths: ADD_PLACE, GET_PLACE, DELETE_PLACE
│       │   │   ├── TestDataBuilder.java      # Builds Add Place and Delete Place payloads
│       │   │   └── Utils.java                # Request/response specs, properties reader, JSON path helper
│       │   ├── Features/
│       │   │   └── PlaceApi.feature          # Place API scenarios
│       │   ├── StepDefinition/
│       │   │   └── StepDefinition.java       # Step definitions for the feature files
│       │   └── cucumber/Options/
│       │       └── TestRunner.java           # JUnit runner: features path, glue, report plugins
│       └── resources/
│           └── logback-test.xml              # Log format and outputs
├── CLAUDE.md                                 # Project context and rules for Claude Code
├── pom.xml                                   # Dependencies and plugins
└── README.md
```

Generated and never committed: `target/` (build output and logs) and `requestLogs.txt`.

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

Tags: `@PlaceApi` (every Place API scenario), `@AddPlace`, `@Smoke`.

**Note:** `pom.xml` sets `testFailureIgnore = true`, so Maven reports **BUILD SUCCESS even when scenarios fail**.
Always check the test results in the console (`pretty` output) or the report.

---

## How the Framework Works

### Configuration and endpoints

Settings live in `Global.properties` and are read only through `Utils.getPropertiesValue("key")`. A missing key
returns `null` and logs a warning.

Endpoint paths live in `GlobalEnum`, relative to the base URI:

```java
ADD_PLACE("/maps/api/place/add/json"),
GET_PLACE("/maps/api/place/get/json"),
DELETE_PLACE("/maps/api/place/delete/json");
```

### Request specification

Every request starts from one shared specification, built once per run by `Utils.defaultRequestSpecification()`:

- base URI and API key from `Global.properties`
- JSON content type
- every request and response logged to `requestLogs.txt`

```java
given().spec(defaultRequestSpecification()).body(payload)
```

Every response is checked against the shared `Utils.defaultResponseSpecification()`, which expects JSON content.
Status codes are verified in the steps, so each scenario can expect its own code and the check is logged:

```java
response = requestSpecification.when().post(resource)
        .then().spec(defaultResponseSpecification())
        .extract().response();
```

### Request bodies

`TestDataBuilder` builds the request bodies:

- `addPlacePayload(name, address, phoneNumber, language, website, types)` returns an `AddPlace` POJO, serialized to
  JSON by Jackson. The location (`LATITUDE`, `LONGITUDE`) and `ACCURACY` are fixed test values, defined as constants.
- `deletePlacePayload(placeId)` returns the JSON body `{"place_id": "<placeId>"}`.

```java
TestDataBuilder testDataBuilder = new TestDataBuilder();
AddPlace addPlace = testDataBuilder.addPlacePayload(name, address, phoneNumber, language, website, types);
String deleteBody = testDataBuilder.deletePlacePayload(placeId);
```

POJO fields are camelCase; JSON keys that differ are mapped with `@JsonProperty`, such as
`phoneNumber` → `"phone_number"`.

### Reading responses

`Utils.getJsonPath(response, "path")` reads a value from a JSON response, including nested paths such as
`location.lat`. A missing path returns `null` and logs a warning, so an assertion fails clearly.

### Assertions and logging

Every check goes through `Assertions.assertEquals(description, expected, actual)`, which logs a PASS or FAIL line
with both values and re-throws on failure:

```
19:40:56 | INFO  | PASS | Verify status in response body | Expected: OK | Actual: OK
```

`Assertions.assertHttpOk(response)` checks for HTTP status 200 the same way, and also logs the response body when the
status is wrong, since it usually explains the error.

Other messages go through `Log.logInformation`, `logWarning` and `logError`. Logs are written to the console and
to one file per run in `target/logs/test_run_<timestamp>.log`, as configured in `logback-test.xml`.

### Scenarios and step definitions

Scenarios live in `src/test/java/Features/`, one feature file per API area:

```gherkin
@PlaceApi
Feature: Place API
  Add, get and delete places through the Place API.

  @AddPlace @Smoke
  Scenario: Add a place
    Given Add Place payload with "Test Place 1" "123 Main Street" "9876543210" "English" "https://example.com" "Engineer"
    When User calls "ADD_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
```

Each step maps to a method in `StepDefinition` by its exact text:

- **Given** builds the request from the shared spec and a `TestDataBuilder` payload.
- **When** resolves the endpoint name through `GlobalEnum` and sends the request with the given HTTP method.
- **Then** verifies the response with `Assertions`.

`TestRunner` runs every feature file with JUnit, prints each step (`pretty`), and writes a JSON report to
`target/jsonReports/`.

---

## Contributing

`main` is protected. Every change goes through a pull request reviewed by the code owner.

```bash
git switch main
git pull
git switch -c feature/<name>
git push -u origin feature/<name>
# make changes, in small working steps
git add <files>
git commit -m "Add <what changed>"
git push
```

Then open a pull request into `main` with a **What**, **Changes** and **How to test** section.
Branch names must start with `feature/`.

---

## Claude Code Skills

- **best-practices:** the project's coding standards (layout, naming, POJOs, config, endpoints, requests,
  assertions, logging, Javadoc, Git workflow). Claude Code applies it automatically when writing or changing code.
- **test-review:** reviews feature files, step definitions and framework code against those standards, including
  that every step matches its definition and uses its parameters. Run `/test-review` for everything, or
  `/test-review <file>` for one file.