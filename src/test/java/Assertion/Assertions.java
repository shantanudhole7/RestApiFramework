package Assertion;

import static Logger.Log.*;

import static org.junit.Assert.assertEquals;

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
	public static <T> void assert_equals(String description, T expected, T actual) {
		try {
			assertEquals(description, expected, actual);
		} catch (AssertionError e) {
			Log_Error(String.format("FAIL | %s | Expected: %s | Actual: %s", description, expected, actual));
			throw e;
		}
		Log_Information(String.format("PASS | %s | Expected: %s | Actual: %s", description, expected, actual));
	}
}