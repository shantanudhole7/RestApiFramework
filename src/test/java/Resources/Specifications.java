package Resources;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static Logger.Log.logInformation;
import static Resources.Utils.getPropertiesValue;

/**
 * Request and response specifications shared by every API call. Each one is built on first use and reused for
 * the whole run. Static methods only; use them through static imports.
 */
public final class Specifications {

	/** Header whose value is masked as [ BLACKLISTED ] in requestLogs.txt and the Allure report. */
	public static final String AUTHORIZATION_HEADER = "Authorization";

	/** File that every logged request and response is written to. */
	private static final String REQUEST_LOG_FILE = "requestLogs.txt";

	/** Request specification shared by every logged API call. */
	private static RequestSpecification defaultRequestSpecification;

	/** Response specification shared by every API call. */
	private static ResponseSpecification defaultResponseSpecification;

	/** Request specification without logging or report filters, for requests that carry credentials. */
	private static RequestSpecification loginRequestSpecification;

	/** Stream to requestLogs.txt, shared by every logged spec so they all write to one file. */
	private static PrintStream requestLogStream;

	/** E-commerce API request specification; built on first use, then reused. */
	private static RequestSpecification ecomRequestSpecification;

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
			defaultRequestSpecification = new RequestSpecBuilder()
					.setBaseUri(baseUri)
					.addQueryParam("key", getPropertiesValue("api.key"))
					.setContentType(ContentType.JSON)
					.addFilter(maskedRequestLoggingFilter())
					.addFilter(ResponseLoggingFilter.logResponseTo(requestLogStream()))
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
	 * @return the login RequestSpecification (no logging, no report filters)
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

	/**
	 * Build the e-commerce API request specification: base URI, request/response logging to requestLogs.txt and
	 * the Allure report, with the Authorization header masked in both. No content type, because product requests
	 * are multipart; the caller adds the Authorization header from the logged-in session.
	 * Built once per run and reused.
	 *
	 * @return the e-commerce RequestSpecification
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public static synchronized RequestSpecification ecomRequestSpecification() throws IOException {
		if (ecomRequestSpecification == null) {
			ecomRequestSpecification = new RequestSpecBuilder()
					.setBaseUri(getPropertiesValue("api.base.uri"))
					.setConfig(RestAssuredConfig.config()
					                            .logConfig(LogConfig.logConfig()
					                                                .blacklistHeader(AUTHORIZATION_HEADER)))
					.addFilter(maskedRequestLoggingFilter())
					.addFilter(ResponseLoggingFilter.logResponseTo(requestLogStream()))
					.addFilter(new AllureRestAssured())
					.build();
		}
		return ecomRequestSpecification;
	}

	/** Open requestLogs.txt once per run, so every logged spec writes to the same file. */
	private static synchronized PrintStream requestLogStream() throws IOException {
		if (requestLogStream == null) {
			// Kept open for the whole run; closed automatically when the JVM exits.
			requestLogStream = new PrintStream(Files.newOutputStream(Paths.get(REQUEST_LOG_FILE)));
		}
		return requestLogStream;
	}

	/**
	 * Request-logging filter for requestLogs.txt that masks the Authorization header. The filter keeps its own
	 * blacklist and does not read the spec's LogConfig (which only AllureRestAssured uses), so it is set here too.
	 */
	private static RequestLoggingFilter maskedRequestLoggingFilter() throws IOException {
		return new RequestLoggingFilter(LogDetail.ALL, true, requestLogStream(), true,
		                                Collections.singleton(AUTHORIZATION_HEADER));
	}
}