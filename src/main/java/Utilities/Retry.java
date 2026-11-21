package Utilities;

import io.restassured.response.Response;

import java.io.IOException;

import static Logger.Log.logWarning;
import static Utilities.ConfigReader.getPropertiesValue;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

/**
 * Sends an API call again when it fails for a temporary reason (5xx or a network error), up to cleanup.retry.attempts
 * tries with cleanup.retry.delay.ms between them.
 */
public final class Retry {

	private static final String ATTEMPTS_KEY = "cleanup.retry.attempts";
	private static final String DELAY_KEY = "cleanup.retry.delay.ms";

	private Retry() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Send the call; if it fails with a server error (5xx) or a network error, wait and send it again,
	 * up to cleanup.retry.attempts tries in total. 4xx and other responses are returned at once, never retried.
	 *
	 * @param call        the API call to send, e.g. {@code () -> orderApi.deleteOrder(orderId, session)}
	 * @param description what the call does, for the log, e.g. "delete order 6afc…"
	 * @return the last response; still a 5xx if every attempt failed with one
	 * @throws IOException              the last network error, or if Global.properties cannot be read
	 * @throws IllegalStateException    if a cleanup.retry.* setting is missing from Global.properties
	 * @throws IllegalArgumentException if cleanup.retry.attempts is less than 1, or a cleanup.retry.* setting is not a
	 *                                  number
	 */
	public static Response sendWithRetry(ApiCall call, String description) throws IOException {
		int attempts = Integer.parseInt(requiredSetting(ATTEMPTS_KEY));
		long delayMs = Long.parseLong(requiredSetting(DELAY_KEY));
		return sendWithRetry(call, description, attempts, delayMs);
	}

	/**
	 * Send the call; if it fails with a server error (5xx) or a network error, wait and send it again,
	 * up to {@code attempts} tries in total. 4xx and other responses are returned at once, never retried.
	 * Package-private so RetryTest can pass its own settings.
	 *
	 * @param call        the API call to send, e.g. {@code () -> orderApi.deleteOrder(orderId, session)}
	 * @param description what the call does, for the log, e.g. "delete order 6afc…"
	 * @param attempts    total tries, including the first; at least 1
	 * @param delayMs     wait between tries, in milliseconds; 0 in unit tests
	 * @return the last response; still a 5xx if every attempt failed with one
	 * @throws IOException              the call's last error, if every attempt threw one
	 * @throws IllegalArgumentException if attempts is less than 1
	 */
	static Response sendWithRetry(ApiCall call, String description, int attempts, long delayMs) throws IOException {
		if (attempts < 1) {
			throw new IllegalArgumentException("Retry attempts must be at least 1, was " + attempts);
		}
		for (int attempt = 1; ; attempt++) {                 // no end condition here: every path inside returns or throws
			String problem;
			try {
				Response response = call.call();
				if (!isServerError(response) || attempt == attempts) {
					return response;                         // success, 4xx, or out of attempts: the hook decides
				}
				problem = "status " + response.statusCode();
			} catch (IOException e) {
				if (attempt == attempts) {
					throw e;                                  // out of attempts: the hook's catch logs it
				}
				problem = e.getMessage();
			}
			logWarning("Cleanup: " + description + " failed (" + problem + "), attempt " + attempt + " of "
					           + attempts + ", retrying in " + delayMs + " ms");
			pause(delayMs);
		}
	}

	/** @return true for a server error (5xx), the only status worth retrying */
	private static boolean isServerError(Response response) {
		return response.statusCode() >= HTTP_INTERNAL_ERROR;
	}

	/**
	 * @param key a key in Global.properties
	 * @return its value
	 * @throws IOException if Global.properties cannot be read
	 */
	private static String requiredSetting(String key) throws IOException {
		String value = getPropertiesValue(key);
		if (value == null) {
			throw new IllegalStateException("Add '" + key + "' to Global.properties");
		}
		return value;
	}

	/**
	 * Wait before the next attempt. If the run is being stopped, keep the stop signal and stop retrying.
	 *
	 * @param delayMs how long to wait, in milliseconds
	 */
	private static void pause(long delayMs) {
		try {
			Thread.sleep(delayMs);
		} catch (InterruptedException e) {
			Thread.currentThread()
			      .interrupt();
			throw new IllegalStateException("Retry interrupted while waiting", e);
		}
	}
}
