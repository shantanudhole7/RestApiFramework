package Assertion;

import io.restassured.response.Response;

import java.util.List;
import java.util.Objects;

import static Logger.Log.logError;
import static Logger.Log.logInformation;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Logging assertions: every check logs a PASS or FAIL line with expected and actual values.
 */
public final class Assertions {

	/** What the check reports when every file follows the rule. */
	private static final String NO_VIOLATIONS = "no violations";

	private Assertions() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Assert that two values are equal, and log the result as a PASS or FAIL line.
	 * On failure the FAIL line is logged first, then an AssertionError is thrown so the test fails. Uses no test
	 * library, so the framework works under any test runner.
	 *
	 * @param description what is being verified, e.g. "Verify status is OK"
	 * @param expected    the expected value
	 * @param actual      the actual value
	 * @param <T>         type of the values being compared
	 */
	public static <T> void assertEquals(String description, T expected, T actual) {
		if (!Objects.equals(expected, actual)) {
			String message = String.format("FAIL | %s | Expected: %s | Actual: %s", description, expected, actual);
			logError(message);
			throw new AssertionError(message);
		}
		logInformation(String.format("PASS | %s | Expected: %s | Actual: %s", description, expected, actual));
	}

	/**
	 * Assert that an API response has the expected HTTP status, and log the result as a PASS or FAIL line.
	 * On failure the status is logged with a pointer to requestLogs.txt, which holds the response unless it carries
	 * personal data.
	 *
	 * @param response       the API response to check
	 * @param expectedStatus the expected HTTP status code, e.g. 200
	 */
	public static void assertHttpStatus(Response response, int expectedStatus) {
		int statusCode = response.statusCode();
		if (statusCode != expectedStatus) {
			String mismatch = "Unexpected status " + statusCode + " (expected " + expectedStatus + ")";
			logError(mismatch + "; see requestLogs.txt for the response");
		}
		assertEquals("Verify that api returns " + expectedStatus, expectedStatus, statusCode);
	}

	/**
	 * Assert that an API response has HTTP status 200 (OK).
	 *
	 * @param response the API response to check
	 */
	public static void assertHttpOk(Response response) {
		assertHttpStatus(response, HTTP_OK);
	}

	/**
	 * Assert that an API response has HTTP status 404 (Not Found), e.g. when fetching a deleted resource.
	 *
	 * @param response the API response to check
	 */
	public static void assertHttpNotFound(Response response) {
		assertHttpStatus(response, HTTP_NOT_FOUND);
	}

	/**
	 * Assert that a check found no violations. On failure the FAIL line shows how many there are, one per line,
	 * so every problem is visible in one run.
	 *
	 * @param description what is checked, e.g. "Verify every feature has an owner and a feature tag"
	 * @param violations  one message per violation; empty if the check holds
	 */
	public static void assertNoViolations(String description, List<String> violations) {
		String newLine = System.lineSeparator();
		String actual = violations.isEmpty()
				? NO_VIOLATIONS
				: violations.size() + " violation(s):" + newLine + "  - " + String.join(newLine + "  - ", violations);
		assertEquals(description, NO_VIOLATIONS, actual);
	}
}