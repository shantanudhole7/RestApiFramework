package Specification;

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
import static Utilities.ConfigReader.getPropertiesValue;

/**
 * Shared request and response specifications.
 * Each spec is built once per run and reused. The default spec logs every request and response to
 * requestLogs.txt; the login spec logs nothing, because it carries credentials.
 * The e-commerce spec logs too, with the Authorization header masked.
 * The status-only e-commerce spec logs only the response status, for responses that carry personal data.
 */
public final class Specifications {

	/** File that every request and response is logged to. */
	private static final String REQUEST_LOG_FILE = "requestLogs.txt";

	/** Header whose value is masked as [ BLACKLISTED ] in requestLogs.txt. */
	public static final String AUTHORIZATION_HEADER = "Authorization";

	/** Request specification shared by every API call; built on first use, then reused for the whole run. */
	private static RequestSpecification requestSpecification;

	/** Response specification shared by every API call; built on first use, then reused for the whole run. */
	private static ResponseSpecification responseSpecification;

	/** Request specification without logging or report filters, for requests that carry credentials. */
	private static RequestSpecification loginRequestSpecification;

	/** Stream to requestLogs.txt, shared by every logged spec so they all write to one file. */
	private static PrintStream requestLogStream;

	/** E-commerce API request specification; built on first use, then reused. */
	private static RequestSpecification ecomRequestSpecification;

	/** E-commerce spec for responses that carry personal data: response status only; built once. */
	private static RequestSpecification ecomStatusOnlyRequestSpecification;

	private Specifications() {
		// Prevent instantiation of this utility class
	}

	/**
	 * Build the base request specification shared by every Place API call: base URI and API key from Global.properties,
	 * JSON content type, request/response logging to requestLogs.txt.
	 * Built once per run and reused, so all calls log to the same file.
	 *
	 * @return the shared RequestSpecification
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public static RequestSpecification defaultRequestSpecification() throws IOException {
		if (requestSpecification == null) {
			String baseUri = getPropertiesValue("api.base.uri");
			requestSpecification = new RequestSpecBuilder().setBaseUri(baseUri)
			                                               .addQueryParam("key", getPropertiesValue("api.key"))
			                                               .setContentType(ContentType.JSON)
			                                               .addFilter(RequestLoggingFilter.logRequestTo(
					                                               requestLogStream()))
			                                               .addFilter(ResponseLoggingFilter.logResponseTo(
					                                               requestLogStream()))
			                                               .build();
			logInformation(
					"Request specification created | Base URI: " + baseUri + " | Requests and responses logged to " + REQUEST_LOG_FILE);
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
			responseSpecification = new ResponseSpecBuilder().expectContentType(ContentType.JSON)
			                                                 .build();
		}
		return responseSpecification;
	}

	/**
	 * Built once per run and reused a request specification for requests that carry credentials, such as login:
	 * base URI and JSON content type only, with no logging, so passwords and tokens never reach requestLogs.txt.
	 *
	 * @return the login RequestSpecification (no logging, no report filters)
	 * @throws IOException if Global.properties cannot be read
	 */
	public static RequestSpecification loginRequestSpecification() throws IOException {
		if (loginRequestSpecification == null) {
			loginRequestSpecification = new RequestSpecBuilder().setBaseUri(getPropertiesValue("api.base.uri"))
			                                                    .setContentType(ContentType.JSON)
			                                                    .build();
		}
		return loginRequestSpecification;
	}

	/**
	 * Open requestLogs.txt once per run, so every logged spec writes to the same file.
	 *
	 * @return the shared stream to requestLogs.txt
	 * @throws IOException if requestLogs.txt cannot be created
	 */
	private static synchronized PrintStream requestLogStream() throws IOException {
		if (requestLogStream == null) {
			// Kept open for the whole run; closed automatically when the JVM exits.
			requestLogStream = new PrintStream(Files.newOutputStream(Paths.get(REQUEST_LOG_FILE)));
		}
		return requestLogStream;
	}

	/**
	 * Build a config that masks the Authorization header wherever REST Assured's own logging runs (log()), kept as a
	 * safeguard when debugging.
	 *
	 * @return the masked RestAssuredConfig
	 */
	private static RestAssuredConfig maskedConfig() {
		return RestAssuredConfig.config()
		                        .logConfig(LogConfig.logConfig()
		                                            .blacklistHeader(AUTHORIZATION_HEADER));
	}

	/**
	 * Build a request logger that writes every request to requestLogs.txt with the Authorization header masked.
	 *
	 * @return the masked RequestLoggingFilter
	 * @throws IOException if requestLogs.txt cannot be created
	 */
	private static RequestLoggingFilter maskedRequestLogging() throws IOException {
		return new RequestLoggingFilter(LogDetail.ALL, true, requestLogStream(), true,
		                                Collections.singleton(AUTHORIZATION_HEADER));
	}

	/**
	 * Build the e-commerce API request specification: base URI, request/response logging to requestLogs.txt, with the
	 * Authorization header masked. No content type, because product requests are multipart; the caller adds the
	 * Authorization header from the logged-in session. Built once per run and reused.
	 *
	 * @return the e-commerce RequestSpecification
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public static synchronized RequestSpecification ecomRequestSpecification() throws IOException {
		if (ecomRequestSpecification == null) {
			ecomRequestSpecification = new RequestSpecBuilder().setBaseUri(getPropertiesValue("api.base.uri"))
			                                                   .setConfig(maskedConfig())
			                                                   .addFilter(maskedRequestLogging())
			                                                   .addFilter(ResponseLoggingFilter.logResponseTo(
					                                                   requestLogStream()))
			                                                   .build();
			logInformation("E-commerce request specification created");
		}
		return ecomRequestSpecification;
	}

	/**
	 * Build the e-commerce request specification for responses that carry personal data (the Order API's responses
	 * hold the account email): the request is logged with the token masked, and the response only by its status line.
	 * Built once per run and reused.
	 *
	 * @return the status-only e-commerce RequestSpecification
	 * @throws IOException if Global.properties cannot be read or requestLogs.txt cannot be created
	 */
	public static synchronized RequestSpecification ecomStatusOnlyRequestSpecification() throws IOException {
		if (ecomStatusOnlyRequestSpecification == null) {
			ecomStatusOnlyRequestSpecification = new RequestSpecBuilder().setBaseUri(getPropertiesValue("api.base.uri"))
			                                                             .setConfig(maskedConfig())
			                                                             .addFilter(maskedRequestLogging())
			                                                             .addFilter(new ResponseLoggingFilter(
					                                                             LogDetail.STATUS, requestLogStream()))
			                                                             .build();
			logInformation("E-commerce status-only request specification created");
		}
		return ecomStatusOnlyRequestSpecification;
	}
}
