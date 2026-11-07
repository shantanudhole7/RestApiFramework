package Assertion;

import io.restassured.response.Response;
import org.junit.Assert;

import static Logger.Log.logError;
import static Logger.Log.logInformation;

/**
 * Logging assertions: every check logs a PASS or FAIL line with expected and actual values.
 */
public class Assertions {

	/**
	 * HTTP status for a successful request.
	 */
	private static final int HTTP_OK = 200;

	/**
	 * HTTP status for a resource that doesn't exist.
	 */
	private static final int HTTP_NOT_FOUND = 404;

	/**
	 * Assert that two values are equal, and log the result as a PASS or FAIL line.
	 * On failure the FAIL line is logged first, then the AssertionError is re-thrown so the test still fails.
	 *
	 * @param description what is being verified, e.g. "Verify status is OK"
	 * @param expected    the expected value
	 * @param actual      the actual value
	 * @param <T>         type of the values being compared
	 */
	public static <T> void assertEquals(String description, T expected, T actual) {
		try {
			Assert.assertEquals(description, expected, actual);
		} catch (AssertionError e) {
			logError(String.format("FAIL | %s | Expected: %s | Actual: %s", description, expected, actual));
			throw e;
		}
		logInformation(String.format("PASS | %s | Expected: %s | Actual: %s", description, expected, actual));
	}

	/**
	 * Assert that an API response has the expected HTTP status, and log the result as a PASS or FAIL line.
	 * On failure the response body is logged too, since it usually explains the error.
	 *
	 * @param response       the API response to check
	 * @param expectedStatus the expected HTTP status code, e.g. 200
	 */
	public static void assertHttpStatus(Response response, int expectedStatus) {
		int statusCode = response.statusCode();
		if (statusCode != expectedStatus) {
			logError("Unexpected status " + statusCode + " | Body: " + response.asString());
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
}