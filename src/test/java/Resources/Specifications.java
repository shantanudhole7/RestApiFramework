package Resources;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Logger.Log.logInformation;
import static Resources.Utils.getPropertiesValue;

/**
 * Request and response specifications shared by every API call. Each one is built on first use and reused for
 * the whole run. Static methods only; use them through static imports.
 */
public final class Specifications {

	/**
	 * File that every logged request and response is written to.
	 */
	private static final String REQUEST_LOG_FILE = "requestLogs.txt";

	/**
	 * Request specification shared by every logged API call.
	 */
	private static RequestSpecification defaultRequestSpecification;

	/**
	 * Response specification shared by every API call.
	 */
	private static ResponseSpecification defaultResponseSpecification;

	/**
	 * Request specification without logging or report filters, for requests that carry credentials.
	 */
	private static RequestSpecification loginRequestSpecification;


	private Specifications() {
		// Static helpers only; never instantiated
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
	public static synchronized RequestSpecification defaultRequestSpecification() throws IOException {
		if (defaultRequestSpecification == null) {
			String baseUri = getPropertiesValue("api.base.uri");
			// Kept open for the whole run; closed automatically when the JVM exits.
			PrintStream requestLogs = new PrintStream(Files.newOutputStream(Paths.get(REQUEST_LOG_FILE)));
			defaultRequestSpecification = new RequestSpecBuilder()
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
		return defaultRequestSpecification;
	}

	/**
	 * Build the base response specification shared by every API call: expect JSON content type.
	 * Built once per run and reused.
	 *
	 * @return the shared ResponseSpecification
	 */
	public static synchronized ResponseSpecification defaultResponseSpecification() {
		if (defaultResponseSpecification == null) {
			defaultResponseSpecification = new ResponseSpecBuilder()
					.expectContentType(ContentType.JSON)
					.build();
		}
		return defaultResponseSpecification;
	}

	/**
	 * Build a request specification for requests that carry credentials, such as login: base URI and JSON content
	 * type only, with no logging and no Allure filter, so passwords and tokens never reach a log file or a report.
	 * Built once per run and reused.
	 *
	 * @return the unlogged RequestSpecification
	 * @throws IOException if Global.properties cannot be read
	 */
	public static synchronized RequestSpecification loginRequestSpecification() throws IOException {
		if (loginRequestSpecification == null) {
			loginRequestSpecification = new RequestSpecBuilder()
					.setBaseUri(getPropertiesValue("api.base.uri"))
					.setContentType(ContentType.JSON)
					.build();
		}
		return loginRequestSpecification;
	}
}