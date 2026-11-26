# RestApiFramework – REST Assured API Test Automation Framework

API test automation framework for the [Rahul Shetty Academy](https://rahulshettyacademy.com) **Place API** and **e-commerce (Product and Order) API**,
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
| Reporting Labs   | One-file HTML report: scenarios, steps, API calls, history       |
| SLF4J + Logback  | Framework logging: console and one log file per run              |

---

## Project Structure

```
RestApiFramework/
├── .claude/skills/                       # best-practices (coding standards), test-review (/test-review)
├── .github/CODEOWNERS                    # Every pull request needs the code owner's review
├── src/
│   ├── main/java/                        # THE FRAMEWORK: what the tests use; no test code here
│   │   ├── Api/                          # One class per API (<Name>Api): builds and sends requests, no assertions;
│   │   │                                 # ApiClient sends step requests and registers created ids for cleanup;
│   │   │                                 # LoginApi logs in once per run and shares the session
│   │   ├── ApiDataBuilder/               # One Test Data Builder per API (<Name>ApiDataBuilder): custom or default payloads
│   │   ├── Assertion/                    # Logging assertions (PASS/FAIL) and HTTP status checks; no test library
│   │   ├── Cleanup/                      # OrphanSweep: deletes test data that earlier runs left behind
│   │   ├── Context/                      # TestContext (per-scenario state), AuthSession (the run's session)
│   │   ├── Logger/                       # The shared logger: logInformation, logWarning, logError
│   │   ├── Pojo/                         # Request models (JSON via Jackson; Product is sent as multipart)
│   │   ├── Resources/
│   │   │   └── GlobalEnum.java           # Every endpoint path; create endpoints declare their id path
│   │   ├── Setup/                        # Setup data shared by hooks and steps (<Name>Setup): create, register, check
│   │   ├── Specification/                # Shared request and response specs: Place, login, e-commerce
│   │   └── Utilities/                    # ConfigReader (settings, secrets), ResponseUtils (JSON),
│   │                                     # Retry + ApiCall (resend a failed cleanup call)
│   └── test/                             # THE TESTS
│       ├── java/
│       │   ├── cucumber/Options/
│       │   │   └── TestRunner.java       # JUnit runner: features path, glue, report plugins
│       │   ├── StepDefinition/           # One step class per API (<Name>ApiStepDefinition), plus Common and Login steps
│       │   │   └── Hooks/                # One hook class per API (<Name>Hooks): setup by tag, cleanup after every scenario;
│       │   │                             # CommonHooks: once per run, before all scenarios (the orphan sweep)
│       │   ├── Cleanup/                  # Unit tests of framework code, in the package they test (OrphanSweepTest)
│       │   └── Utilities/                # (RetryTest)
│       └── resources/                    # Non-Java files the tests read, loaded from the classpath
│           ├── features/                 # One feature file per API (<Name>Api.feature)
│           ├── files/                    # Test files, e.g. the placeholder image uploaded by Create Product
│           ├── Global.properties         # Settings: base URI, API key, cleanup retry, orphan sweep age
│           ├── secrets.properties.example   # Template for the git-ignored secrets.properties
│           ├── logback-test.xml          # Log format and outputs
│           └── reporting-labs.properties # Reporting Labs: title, BDD steps, masked keys
├── CLAUDE.md                             # Project context and rules for Claude Code
├── pom.xml                               # Dependencies and plugins
└── README.md
```

`src/main/java` is the framework; `src/test` holds the tests that use it: the runner, the step definitions and
hooks, unit tests, and the feature files and config they read.

Generated and never committed: `target/` (build output, logs and reports), `requestLogs.txt`,
and `reporting-labs.history.json` (Reporting Labs run history).
Also never committed: `secrets.properties`, your local credentials.

---

## Setup

**1. Prerequisites:** JDK 11 or newer, and Maven, either installed on the PATH or the one built into IntelliJ IDEA.

**2. Clone and open**

```bash
git clone https://github.com/shantanudhole7/RestApiFramework.git
```

Open the folder in IntelliJ IDEA as a Maven project and let it download the dependencies.

**3. Check `src/test/resources/Global.properties`**

```properties
# Base URI of the Place API; endpoint paths come from GlobalEnum
api.base.uri=https://rahulshettyacademy.com

# Public demo key for the practice API, sent as the "key" query parameter
api.key=qaclick123
```

Write values without quotes. Keys are case-sensitive.

**4. Create `src/test/resources/secrets.properties`** (Product API only)

Copy `secrets.properties.example` to `secrets.properties` in the same folder and fill in a test account for the
e-commerce practice site. The file is git-ignored, so the credentials never reach the repository:

```properties
ecom.user.email=your-test-account@example.com
ecom.user.password=your-password
```

**5. Build**

```bash
mvn test-compile
```

Without Maven on the PATH, use IntelliJ's **Maven tool window → Lifecycle → test-compile**. It should end with **BUILD
SUCCESS**.

---

## Running Tests

Config and feature files are loaded from the classpath, so tests can be started from any folder. The scenarios call
the live practice API; the unit tests (`RetryTest`, `OrphanSweepTest`) don't call any API.

| Goal                  | Command                                                       |
|-----------------------|---------------------------------------------------------------|
| All scenarios         | `mvn test`, or run `TestRunner` in IntelliJ                   |
| By tag                | `mvn test -Dcucumber.filter.tags="@AddPlace"`                 |
| One scenario          | Green arrow next to the scenario in the `.feature` file       |
| Unit tests            | Green arrow next to `RetryTest` or `OrphanSweepTest`; `mvn test` runs them too |

Tags: `@PlaceApi`, `@ProductApi` and `@OrderApi` (every scenario of that API), `@AddPlace`, `@DeletePlace`,
`@CreateProduct`, `@DeleteProduct`, `@CreateOrder`, `@DeleteOrder`, `@Smoke`.

Metadata tags for the Reporting Labs report use the library's own shapes: `@owner:Shantanu` and `@feature:Place`
(on each Feature), `@P1 @critical` (create scenarios) and `@P2 @major` (delete scenarios). They also filter runs:
`mvn test "-Dcucumber.filter.tags=@P1"`.

**Note:** `pom.xml` sets `testFailureIgnore = true`, so Maven reports **BUILD SUCCESS even when scenarios fail**.
Always check the test results in the console (`pretty` output) or the report.

### Reports

Run `TestRunner` (or `mvn test`), then:

| Report         | How to open                                               | What it shows                                                                                                        |
|----------------|-----------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| Cucumber HTML  | Open `target/cucumber-reports/cucumber.html` in a browser | Quick single-file overview, filterable by tag                                                                        |
| Reporting Labs | Open `target/reporting-labs/index.html` in a browser      | Scenarios, Given/When/Then steps, every API call (personal data masked), priority/owner/feature, history across runs |

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

`Specifications` has four request specs:

| Spec                                   | Used for    | What it adds                                                                |
|----------------------------------------|-------------|-----------------------------------------------------------------------------|
| `defaultRequestSpecification()`        | Place API   | API key, JSON, logging to `requestLogs.txt`                                 |
| `ecomRequestSpecification()`           | Product API | Logging with the `Authorization` header masked; no content type (multipart) |
| `ecomStatusOnlyRequestSpecification()` | Order API   | Request logged (token masked), response only by its status line             |
| `loginRequestSpecification()`          | Login       | JSON only: no logging and no report, so credentials never reach a file      |

The token is masked by a `RequestLoggingFilter` blacklist for `requestLogs.txt`, plus a `LogConfig` blacklist that
masks it wherever REST Assured's own logging runs (`log()`, a safeguard when debugging). Both e-commerce specs build
them with the same private helpers (`maskedConfig()`, `maskedRequestLogging()`), so the mask exists once.

Order API responses hold the account's email address (`orderBy`), so the Order API uses the status-only spec: its
responses are never written to `requestLogs.txt`. The tests still read the full
response; logging filters only write a copy. To see an order response while debugging, repeat the call in Postman.

The Reporting Labs report records every REST Assured call (its add-on adds a global filter) and **can** mask single
fields, so order calls appear there with `orderBy` masked (`reporting-labs.maskKeys=orderBy,userEmail` in
`reporting-labs.properties`; tokens and passwords are masked by default). The rule behind both: personal data never
appears in any output; where an output can mask a field it is masked, and where it can't, the body is left out.

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
with both values and throws an `AssertionError` on failure:

```
19:40:56 | INFO  | PASS | Verify status in response body | Expected: OK | Actual: OK
```

`Assertions.assertHttpStatus(response, code)` checks for an exact HTTP status the same way. When the status is wrong,
it logs both status codes and points to `requestLogs.txt`, which holds the response unless it carries personal data;
it never logs the body itself. `assertHttpOk(response)` (200) and
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

Hooks in `StepDefinition/Hooks/` (one class per API) run setup selected by tag. Before every `@DeletePlace` scenario, a hook
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

Hooks call the API through `Api/PlaceApi`, not through step methods. Like `ProductApi` and `OrderApi`, `PlaceApi`
builds unsent requests for steps (`addPlaceRequest`, `getPlaceRequest`, `deletePlaceRequest`) and sends them for hooks
(`addPlace`, `deletePlace`), reusing the same builders. It keeps no state and makes no assertions, so the caller decides
what to check.

After every scenario, passed or failed, an untagged `@After` hook deletes every place the scenario created. Places
are registered in `TestContext` the moment they are created (by `ApiClient` after any call to an endpoint that declares an id path,
and by the setup hook), so a scenario that fails halfway still cleans up. Cleanup never fails a scenario: a place that is
already gone is logged as info, and any other problem as a warning.

```java
@After
public void cleanUpCreatedPlaces() {
    for (String placeId : testContext.getCreatedIds(GlobalEnum.ADD_PLACE)) {
        Response response = sendWithRetry(() -> placeApi.deletePlace(placeId), "delete place " + placeId);
        // 200 deleted, 404 already gone, else warning
    }
}
```

When a delete itself fails, cleanup still never fails the scenario:

- **Retry:** each delete goes through `Retry.sendWithRetry`. A server error (5xx) or a network error is retried after
  a short pause, up to `cleanup.retry.attempts` tries with `cleanup.retry.delay.ms` between them (`Global.properties`).
  A 4xx is never retried, because the request itself is wrong. If every try fails, the hook logs a warning with the
  id and status, plus the body for Place and Product (Order logs the status only: its responses carry personal data).
- **Keep the parent:** if an order still can't be deleted, `ProductHooks` keeps the scenario's products and logs each
  one, so no order is left pointing at a deleted product. `OrderHooks` leaves that note in `TestContext`
  (`markCleanupFailed`), and runs first because of `@After(order = 20000)`.
- The retry rules are covered by unit tests (`Utilities/RetryTest`) with fake responses, because the live API can't be
  made to fail on demand.

### Orphan sweep

Per-scenario cleanup can still leave data behind: a run stopped halfway, or a delete that failed every retry. Once per
run, before the first scenario, `CommonHooks` (`@BeforeAll`) runs `Cleanup/OrphanSweep`, which deletes what earlier runs
left on the e-commerce API. It deletes only what is certainly test data from this framework; anything uncertain is kept:

- **Ours:** a product must be added by the logged-in test user (`productAddedBy`) **and** have a name starting with
  `ProductApiDataBuilder.TEST_PRODUCT_PREFIX` (`Test Product`). The data builder refuses any other product name, so
  every product the tests create carries the prefix. Orders are checked by prefix only, because the order list is
  already the test user's own.
- **Old enough:** the first 8 hex characters of a MongoDB id are its creation time, so only data older than
  `sweep.min.age.minutes` (30, in `Global.properties`) is deleted. A run in progress on another machine is never touched.
- **Orders first, and no product an order still needs:** every order that still exists after the order pass (too young,
  not a test order, or its delete failed) protects its product (`productOrderedId`), which is kept and logged as
  `kept, an order still references it`. If the orders can't be read at all, every product is kept.
- **Never fails the run:** a failed login, fetch or delete is logged as a warning, and the run goes on. The sweep and
  its hook catch `Exception | AssertionError`, because the shared response spec throws `AssertionError` for a non-JSON
  response. Deletes go through `Retry.sendWithRetry`, like the per-scenario cleanup.
- **Logs ids and names only**, never whole orders: an order holds the account's email address.

The sweep's lines start with `Orphan sweep:`, for example `Orphan sweep: deleted 1 product(s), 0 failed`. The selection
rules are covered by unit tests (`Cleanup/OrphanSweepTest`) with fake ids and a fixed clock.

### Scenarios and step definitions

Scenarios live in `src/test/resources/features/`, one feature file per API area. `TestRunner` loads them with
`features = "classpath:features"`:

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
    # The @DeletePlace hook creates the place to delete (PlaceHooks.createPlaceForDelete)
    Given Delete Place payload for the created place
    When User calls "DELETE_PLACE" with "POST" http request
    Then the API call succeeds with status code 200
    And "status" in response body is "OK"
    And the deleted place can no longer be fetched using "GET_PLACE"
```

Each step maps to a method in `PlaceApiStepDefinition` by its exact text or, for the generic steps (`User calls…`, `status code`, `response body`), `CommonStepDefinition`:

- **Given** gets the request from `PlaceApi` (built from the shared spec and a `PlaceApiDataBuilder` payload).
- **When** resolves the endpoint name through `GlobalEnum` and sends the request through `ApiClient`.
- **Then** verifies the response with `Assertions`.
- **`the place created maps to "<Name>" using "GET_PLACE"`** reads `place_id` from the response, calls `GET_PLACE`
  with it, and checks the name.
- **`"<key>" in response body is "<value>"`** reads any JSON key from the last response and checks its value.
- **`the deleted place can no longer be fetched using "GET_PLACE"`** calls `GET_PLACE` with the deleted `place_id`
  and expects 404 Not Found.

`TestRunner` runs every feature file with JUnit, prints each step (`pretty`), and writes the Reporting Labs report and
the Cucumber HTML report (see [Reports](#reports)).

---

### Product API

`ProductApi.feature` works as a logged-in user. Its `Background` step (`Given the user is logged in`) takes the session
from `LoginApi`, which logs in once per run with the credentials from `secrets.properties` and shares the immutable
`AuthSession` (token and user id) with every scenario.

```gherkin
  Background:
    Given the user is logged in

  @CreateProduct @Smoke
  Scenario Outline: Create a product and verify it can be fetched
    Given Create Product payload with "<Name>" "<Category>" "<Sub Category>" "<Description>" "<Price>" "<For>"
    When User calls "CREATE_PRODUCT" with "POST" http request
    Then the API call succeeds with status code 201
    And "message" in response body is "Product Added Successfully"
    And the product created can be fetched using "GET_PRODUCT"
```

- Create Product is a multipart request: the product's fields plus a placeholder image, with the session's token.
  `ProductApi` builds it; the generic http step sends it through `ApiClient`, which registers the new `productId`
  for cleanup because `CREATE_PRODUCT` declares it in `GlobalEnum`.
- `@Before("@DeleteProduct")` in `ProductHooks` creates the product each Delete scenario deletes. Hooks run before the
  `Background`, so the hook gets the session from `LoginApi` itself.
- This API answers **201** for a created product, and **400** with `Product not found` (not 404) for a missing one;
  on delete the message is spelled `Product not foundd`. The tests assert that actual contract, and product cleanup
  treats it as "already deleted".

### Order API

`OrderApi.feature` also works as a logged-in user. An order needs a product, so the Create scenario creates one first,
as a visible step, because it is part of what is being tested:

```gherkin
  @CreateOrder @Smoke
  Scenario Outline: Create an order and verify it can be fetched
    Given a product exists in the store
    And Place Order payload with country "<Country>"
    When User calls "CREATE_ORDER" with "POST" http request
    Then the API call succeeds with status code 201
    And "message" in response body is "Order Placed Successfully"
    And the order created can be fetched using "GET_ORDER"
    And the fetched order contains the ordered product
```

- Setup data that hooks and steps both need is created by the classes in `Setup/`: `ProductSetup.createDefaultProduct`
  and `OrderSetup.placeDefaultOrder` build, send, register for cleanup, check, and store the id in `TestContext`, so
  that sequence exists once.
- `@Before("@DeleteOrder")` in `OrderHooks` creates a product and places an order for each Delete scenario.
- An order references its product, so cleanup deletes orders first: `@After(order = 20000)` in `OrderHooks` runs
  before the product cleanup (for `@After`, a higher order runs first).
- This API answers **201** for a placed order, with the new id at `orders[0]` (an array). An order is fetched by query
  parameter (`get-orders-details?id=…`) and deleted by path (`delete-order/{orderId}`). A missing order gets **400**
  with `Order not found` on both fetch and delete.

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