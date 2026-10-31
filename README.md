# RestApiFramework – REST Assured API Test Automation Framework

API test automation framework for the [Rahul Shetty Academy](https://rahulshettyacademy.com) **Place API**,
built with **Java**, **REST Assured** and **Maven**.

---

## Tech Stack

| Tool             | Purpose                                                  |
|------------------|----------------------------------------------------------|
| Java 11+         | Language                                                 |
| Maven            | Build and dependencies                                   |
| REST Assured     | HTTP requests, request specification and request logging |
| Jackson          | Serializes POJOs to JSON request bodies                  |
| Cucumber + JUnit | BDD test runner (dependencies in `pom.xml`)              |
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
│       │   └── Resources/
│       │       ├── Global.properties         # Settings: base URI, API key
│       │       ├── GlobalEnum.java           # Endpoint paths: ADD_PLACE, GET_PLACE, DELETE_PLACE
│       │       ├── TestDataBuilder.java      # Builds Add Place and Delete Place payloads
│       │       └── Utils.java                # Request spec, properties reader, JSON path helper
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

Every request starts from one shared specification, built once per run by `Utils.requestSpecification()`:

- base URI and API key from `Global.properties`
- JSON content type
- every request and response logged to `requestLogs.txt`

```java
given().

spec(requestSpecification()).

body(payload)
```

### Request bodies

`TestDataBuilder` builds request bodies. Add Place uses the `AddPlace` and `Location` POJOs with fluent setters,
serialized to JSON by Jackson. JSON keys that aren't camelCase are mapped with `@JsonProperty`, such as
`phoneNumber` → `"phone_number"`.

```java
AddPlace payload = new TestDataBuilder().addPlacePayload(name, address, phoneNumber, language, website, types);
```

```java
new AddPlace().

setName(name).

setAddress(address).

setLocation(new Location().

setLat(-38.38).

setLng(33.42));
```

### Reading responses

`Utils.getJsonPath(response, "path")` reads a value from a JSON response, including nested paths such as
`location.lat`. A missing path returns `null` and logs a warning, so an assertion fails clearly.

### Assertions and logging

Every check goes through `Assertions.assertEquals(description, expected, actual)`, which logs a PASS or FAIL line
with both values and re-throws on failure:

```
19:40:56 | INFO  | PASS | Verify status in response body | Expected: OK | Actual: OK
```

Other messages go through `Log.logInformation`, `logWarning` and `logError`. Logs are written to the console and
to one file per run in `target/logs/test_run_<timestamp>.log`, as configured in `logback-test.xml`.

---

## Contributing

`main` is protected. Every change goes through a pull request reviewed by the code owner.

```bash
git switch main
git pull
git switch -c feature/<name>
# make changes
git add <files>
git commit -m "Add <what changed>"
git push -u origin feature/<name>
```

Then open a pull request into `main` with a **What**, **Changes** and **How to test** section.
Branch names must start with `feature/`.

---

## Claude Code Skills

- **best-practices:** the project's coding standards (layout, naming, POJOs, config, endpoints, requests,
  assertions, logging, Javadoc, Git workflow). Claude Code applies it automatically when writing or changing code.
- **test-review:** reviews the framework's code against those standards. Run `/test-review` for all code, or
  `/test-review <file>` for one file.