package Api;

import Context.TestContext;
import Resources.GlobalEnum;
import io.restassured.http.Method;
import io.restassured.response.Response;

import static Logger.Log.logInformation;
import static Specification.Specifications.defaultResponseSpecification;
import static Utilities.ResponseUtils.getJsonPath;

/**
 * Sends the scenario's prepared request to an endpoint, checks the shared response spec, stores the response in
 * TestContext, and registers ids of created resources for cleanup
 */
public class ApiClient {

	/** Shared state of the current scenario, injected by Cucumber (cucumber-picocontainer). */
	private final TestContext testContext;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public ApiClient(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Send the prepared request to the given endpoint and check the response against the shared JSON response spec.
	 * If the endpoint declares a created-id path in GlobalEnum, registers the new id in TestContext for cleanup.
	 *
	 * @param endpoint the endpoint to call
	 * @param method   the HTTP method to send
	 * @return the response of the API call
	 */
	public Response send(GlobalEnum endpoint, Method method) {
		logInformation("Send " + method + " " + endpoint);
		Response response = testContext.getRequestSpecification()
		                               .when()
		                               .request(method, endpoint.getResource())
		                               .then()
		                               .spec(defaultResponseSpecification())
		                               .extract()
		                               .response();
		testContext.setResponse(response);

		// Register created resources as soon as they exist, so cleanup deletes them even if a later step fails
		String idPath = endpoint.getCreatedIdPath();
		if (idPath != null) {
			String createdId = getJsonPath(response, idPath);
			if (createdId != null) {
				testContext.addCreatedId(endpoint, createdId);
			}
		}
		return response;
	}
}
