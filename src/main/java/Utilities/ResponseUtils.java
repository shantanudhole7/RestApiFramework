package Utilities;

import io.restassured.response.Response;

import static Logger.Log.logWarning;

/** Shared helpers for the API steps: reading values from JSON responses. */
public final class ResponseUtils {

	private ResponseUtils() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Read a value from a JSON response by its path.
	 * Logs a warning if the path is not in the response.
	 *
	 * @param response the API response to read from
	 * @param key      JSON path of the value, e.g. "status", "place_id" or "location.lat"
	 * @return the value as a string, or null if the path is not in the response
	 */
	public static String getJsonPath(Response response, String key) {
		String value = response.jsonPath()
		                       .getString(key);
		if (value == null) {
			logWarning("JSON path '" + key + "' not found in response | Status: " + response.statusCode());
		}
		return value;
	}
}
