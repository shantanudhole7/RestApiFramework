package Api;

import ApiDataBuilder.LoginApiDataBuilder;
import Context.AuthSession;
import Resources.GlobalEnum;
import io.restassured.response.Response;

import java.io.IOException;

import static Logger.Log.logError;
import static Logger.Log.logInformation;
import static Specification.Specifications.loginRequestSpecification;
import static Utilities.ConfigReader.getSecretValue;
import static Utilities.ResponseUtils.getJsonPath;
import static io.restassured.RestAssured.given;

/**
 * Logs in to the e-commerce API once per run and shares the session (token and user id) with every scenario.
 * Credentials come from secrets.properties (git-ignored), read with ConfigReader.getSecretValue.
 */
public final class LoginApi {

	/** Login API status for a successful login. */
	private static final int HTTP_OK = 200;

	/** secrets.properties key holding the test user's email. */
	private static final String USER_EMAIL_KEY = "ecom.user.email";

	/** secrets.properties key holding the test user's password. */
	private static final String USER_PASSWORD_KEY = "ecom.user.password";

	/**
	 * Session of the run, created by the first login and then reused. Static on purpose: it is run-scoped and
	 * immutable, unlike scenario state, which lives in TestContext.
	 */
	private static AuthSession session;

	/** Prevent instantiation: static methods only */
	private LoginApi() {
	}

	/**
	 * Return the run's session, logging in on first use.
	 * Synchronized on the class, so scenarios running in parallel can never log in twice.
	 *
	 * @return the run's session
	 * @throws IOException if Global.properties or secrets.properties cannot be read
	 * @throws IllegalStateException if a credential is not set, or the login fails or returns no token
	 */
	public static synchronized AuthSession session() throws IOException {
		if (session == null) {
			session = login();
		}
		return session;
	}

	/**
	 * Log in with the credentials from secrets.properties. The request uses the login spec, which has no logging,
	 * so the password and token never reach requestLogs.txt or the Allure report.
	 *
	 * @return the new session
	 * @throws IOException           if Global.properties or secrets.properties cannot be read
	 * @throws IllegalStateException if the login fails or returns no token
	 */
	private static AuthSession login() throws IOException {
		LoginApiDataBuilder loginApiDataBuilder = new LoginApiDataBuilder();
		logInformation("Log in to the e-commerce API with " + GlobalEnum.LOGIN);
		Response response = given().spec(loginRequestSpecification())
		                           .body(loginApiDataBuilder.loginPayload(getSecretValue(USER_EMAIL_KEY),
		                                                                  getSecretValue(USER_PASSWORD_KEY)))
		                           .when()
		                           .post(GlobalEnum.LOGIN.getResource())
		                           .then()
		                           .extract()
		                           .response();

		if (response.statusCode() != HTTP_OK) {
			// Status only: the body of a failed login may echo the email back
			logError("Login failed | Status: " + response.statusCode());
			throw new IllegalStateException("Login failed with status " + response.statusCode() + "; check " +
					                                USER_EMAIL_KEY + " and " + USER_PASSWORD_KEY + " in secrets.properties");
		}
		String token = getJsonPath(response, "token");
		if (token == null || token.trim().isEmpty()) {
			logError("Login returned no token | Status: " + response.statusCode());
			throw new IllegalStateException("Login succeeded but returned no token");
		}
		String userId = getJsonPath(response, "userId");
		logInformation("Logged in as user " + userId);
		return new AuthSession(token, userId);
	}
}