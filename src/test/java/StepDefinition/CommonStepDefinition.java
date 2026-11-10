package StepDefinition;

import Api.ApiClient;
import Context.TestContext;
import Resources.GlobalEnum;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Method;

import java.util.Arrays;
import java.util.Locale;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logError;
import static Logger.Log.logInformation;
import static Utilities.ResponseUtils.getJsonPath;

/**
 * Steps shared by every API feature.
 */
public class CommonStepDefinition {

	/** Shared state of the current scenario, injected by Cucumber (cucumber-picocontainer). */
	private final TestContext testContext;

	/** client that sends prepared requests and checks the responses */
	private final ApiClient apiClient;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 * @param apiClient   client that sends prepared requests and checks the responses
	 */
	public CommonStepDefinition(TestContext testContext, ApiClient apiClient) {
		this.testContext = testContext;
		this.apiClient = apiClient;
	}

	/**
	 * Send the prepared request to the given endpoint and check the response against the shared JSON response spec.
	 * Create endpoints are registered for cleanup by ApiClient.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum, e.g. "ADD_PLACE"
	 * @param method      HTTP method name, case-insensitive, e.g. "POST", "GET", "DELETE"
	 * @throws IllegalArgumentException if the HTTP method is not supported
	 */
	@When("User calls {string} with {string} http request")
	public void userCallsWithHttpRequest(String apiEndPoint, String method) {
		GlobalEnum endpoint = GlobalEnum.valueOf(apiEndPoint);
		logInformation("User calls '" + apiEndPoint + "' with '" + method + "' http request");
		Method httpMethod;
		try {
			httpMethod = Method.valueOf(method.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			logError("Unsupported HTTP method: " + method + " | Supported: " + Arrays.toString(Method.values()));
			throw new IllegalArgumentException("Unsupported HTTP method: " + method, e);
		}
		apiClient.send(endpoint, httpMethod);
	}

	@Then("the API call succeeds with status code {int}")
	public void theApiCallSucceedsWithStatusCode(Integer statusCode) {
		logInformation("Verify the response status code is " + statusCode);
		assertHttpStatus(testContext.getResponse(), statusCode);
	}

	@Then("{string} in response body is {string}")
	public void inResponseBodyIs(String key, String expectedValue) {
		logInformation("Verify that the response body contains " + key + " = " + expectedValue);
		assertEquals("Verify that the response body contains " + key, expectedValue,
		             getJsonPath(testContext.getResponse(), key));
	}
}
