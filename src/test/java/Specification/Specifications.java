package Specification;

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
import static Utilities.ConfigReader.getPropertiesValue;

/**
 * Shared request and response specifications.
 * Each spec is built once per run and reused. The default spec logs every request and response to
 * requestLogs.txt and Allure; the login spec logs nothing, because it carries credentials.
 */
public final class Specifications {

	/** File that every request and response is logged to. */
	private static final String REQUEST_LOG_FILE = "requestLogs.txt";

	/** Request specification shared by every API call; built on first use, then reused for the whole run. */
	private static RequestSpecification requestSpecification;

	/** Response specification shared by every API call; built on first use, then reused for the whole run. */
	private static ResponseSpecification responseSpecification;

	/** Request specification without logging or report filters, for requests that carry credentials. */
	private static RequestSpecification loginRequestSpecification;

	private Specifications() {
		// Prevent instantiation of this utility class
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
	public static RequestSpecification defaultRequestSpecification() throws IOException {
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
	public static ResponseSpecification defaultResponseSpecification() {
		if (responseSpecification == null) {
			responseSpecification = new ResponseSpecBuilder()
					.expectContentType(ContentType.JSON)
					.build();
		}
		return responseSpecification;
	}

	/**
	 * Build a request specification for requests that carry credentials, such as login: base URI and JSON content
	 * type only, with no logging and no Allure filter, so passwords and tokens never reach a log file or a report.
	 * Built once per run and reused.
	 *
	 * @return the login RequestSpecification (no logging, no report filters)
	 * @throws IOException if Global.properties cannot be read
	 */
	public static RequestSpecification loginRequestSpecification() throws IOException {
		if (loginRequestSpecification == null) {
			loginRequestSpecification = new RequestSpecBuilder()
					.setBaseUri(getPropertiesValue("api.base.uri"))
					.setContentType(ContentType.JSON)
					.build();
		}
		return loginRequestSpecification;
	}
}
