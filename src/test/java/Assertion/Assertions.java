package Assertion;

import io.restassured.response.Response;
import org.junit.Assert;

import static Logger.Log.logError;
import static Logger.Log.logInformation;

public class Assertions {

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
	 * Assert that an API response has HTTP status 200 (OK), and log the result as a PASS or FAIL line.
	 * On failure the response body is logged too, since it usually explains the error.
	 *
	 * @param response the API response to check
	 */
	public static void assertHttpOk(Response response) {
		int statusCode = response.statusCode();
		if (statusCode != 200) {
			logError("Unexpected status " + statusCode + " | Body: " + response.asString());
		}
		assertEquals("Verify that api returns 200", 200, statusCode);
	}
}