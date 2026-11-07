package Resources;

import io.qameta.allure.restassured.AllureRestAssured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static Logger.Log.logError;
import static Logger.Log.logInformation;
import static Logger.Log.logWarning;

/**
 * Shared helpers for the API steps: reading settings from Global.properties,
 * building the common request specification, and reading values from JSON responses.
 */
public class Utils {

	/**
	 * Location of Global.properties, relative to the project root (the folder tests are run from).
	 */
	private static final Path PROPERTIES_FILE =
			Paths.get(System.getProperty("user.dir"), "src", "test", "java", "Resources", "Global.properties");

	/**
	 * File that every request and response is logged to.
	 */
	private static final String REQUEST_LOG_FILE = "requestLogs.txt";

	/**
	 * Request specification shared by every API call; built on first use, then reused for the whole run.
	 */
	private static RequestSpecification requestSpecification;

	/**
	 * Response specification shared by every API call; built on first use, then reused for the whole run.
	 */
	private static ResponseSpecification responseSpecification;

	/**
	 * Read a value from Global.properties.
	 * The file is read on every call, so changes take effect without restarting.
	 * Logs a warning if the key is missing, and an error if the file cannot be read.
	 *
	 * @param globalKey the key to look up, e.g. "api.base.uri"
	 * @return the value for the key, or null if the key is not in the file
	 * @throws IOException if Global.properties cannot be found or read
	 */
	public static String getPropertiesValue(String globalKey) throws IOException {
		Properties properties = new Properties();
		try (InputStream inputStream = Files.newInputStream(PROPERTIES_FILE)) {
			properties.load(inputStream);
		} catch (IOException e) {
			logError("Could not read " + PROPERTIES_FILE + " | " + e.getMessage());
			throw e;
		}
		String value = properties.getProperty(globalKey);
		if (value == null) {
			logWarning("Key '" + globalKey + "' not found in " + PROPERTIES_FILE.getFileName());
		}
		return value;
	}

	/**
	 * Build the base request specification shared by every Place API call:
	 * base URI and API key from Global.properties, JSON content type,
	 * request/response logging to requestLogs.txt, and every request and response attached to the Allure report.
	 * Built once per run and reused, so all calls log to the same file.
	 *
	 * @return the shared RequestSpecification
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public RequestSpecification defaultRequestSpecification() throws IOException {
		if (requestSpecification == null) {
			String baseUri = getPropertiesValue("api.base.uri");
			// Kept open for the whole run; closed automatically when the JVM exits.
			PrintStream requestLogs = new PrintStream(Files.newOutputStream(Paths.get(REQUEST_LOG_FILE)));
			requestSpecification = new RequestSpecBuilder()
					.setBaseUri(baseUri)
					.addQueryParam("key", getPropertiesValue("api.key"))
					.setContentType(ContentType.JSON)
					.addFilter(RequestLoggingFilter.logRequestTo(requestLogs))
					.addFilter(ResponseLoggingFilter.logResponseTo(requestLogs))
					.addFilter(new AllureRestAssured())
					.build();
			logInformation("Request specification created | Base URI: " + baseUri
					               + " | Requests and responses logged to " + REQUEST_LOG_FILE);
		}
		return requestSpecification;
	}

	/**
	 * Build the base response specification shared by every Place API call:
	 * expect JSON content type.
	 * Built once per run and reused.
	 *
	 * @return the shared ResponseSpecification
	 */
	public ResponseSpecification defaultResponseSpecification() {
		if (responseSpecification == null) {
			responseSpecification = new ResponseSpecBuilder()
					.expectContentType(ContentType.JSON)
					.build();
		}
		return responseSpecification;
	}

	/**
	 * Read a value from a JSON response by its path.
	 * Logs a warning if the path is not in the response.
	 *
	 * @param response the API response to read from
	 * @param key      JSON path of the value, e.g. "status", "place_id" or "location.lat"
	 * @return the value as a string, or null if the path is not in the response
	 */
	public String getJsonPath(Response response, String key) {
		String value = response.jsonPath()
		                       .getString(key);
		if (value == null) {
			logWarning("JSON path '" + key + "' not found in response | Status: " + response.statusCode());
		}
		return value;
	}
}