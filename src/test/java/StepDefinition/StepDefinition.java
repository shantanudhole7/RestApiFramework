package StepDefinition;

import Context.TestContext;
import Resources.GlobalEnum;
import Resources.TestDataBuilder;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.Method;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpNotFound;
import static Assertion.Assertions.assertHttpOk;
import static Assertion.Assertions.assertHttpStatus;
import static Logger.Log.logError;
import static Logger.Log.logInformation;
import static Resources.Specifications.defaultRequestSpecification;
import static Resources.Specifications.defaultResponseSpecification;
import static Resources.Utils.getJsonPath;
import static io.restassured.RestAssured.given;

/**
 * Step definitions for the Place API feature.
 */
public class StepDefinition {

	private final TestContext testContext;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public StepDefinition(TestContext testContext) {
		this.testContext = testContext;
	}

	@Given("Add Place payload with {string} {string} {string} {string} {string} {string}")
	public void addPlacePayloadWith(String name, String address, String phoneNumber, String language, String website,
	                                String types) throws IOException {
		TestDataBuilder testDataBuilder = new TestDataBuilder();
		logInformation("Build Add Place payload for '" + name + "'");
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .body(testDataBuilder.addPlacePayload(name, address, phoneNumber,
		                                                                                 language, website, types)));
	}

	/**
	 * Send the prepared request to the given endpoint and check the response against the shared JSON response spec.
	 * Also called by other steps, e.g. to fetch a created place with GET.
	 * After an ADD_PLACE call, registers the new place_id in TestContext for the cleanup hook.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum, e.g. "ADD_PLACE"
	 * @param method      HTTP method name, case-insensitive, e.g. "POST", "GET", "DELETE"
	 * @throws IllegalArgumentException if the HTTP method is not supported
	 */
	@When("User calls {string} with {string} http request")
	public void userCallsWithHttpRequest(String apiEndPoint, String method) {
		GlobalEnum endpoint = GlobalEnum.valueOf(apiEndPoint);
		String resource = endpoint.getResource();
		logInformation("User calls '" + apiEndPoint + "' with '" + method + "' http request");

		Method httpMethod;
		try {
			httpMethod = Method.valueOf(method.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			logError("Unsupported HTTP method: " + method + " | Supported: " + Arrays.toString(Method.values()));
			throw new IllegalArgumentException("Unsupported HTTP method: " + method, e);
		}

		Response response = testContext.getRequestSpecification()
		                               .when()
		                               .request(httpMethod, resource)
		                               .then()
		                               .spec(defaultResponseSpecification())
		                               .extract()
		                               .response();
		testContext.setResponse(response);

		// Register new places as soon as they exist, so cleanup deletes them even if a later step fails
		if (endpoint == GlobalEnum.ADD_PLACE) {
			String createdPlaceId = getJsonPath(response, "place_id");
			if (createdPlaceId != null) {
				testContext.addCreatedPlaceId(createdPlaceId);
			}
		}
	}

	@Then("the API call succeeds with status code {int}")
	public void theApiCallSucceedsWithStatusCode(Integer statusCode) {
		logInformation("Verify the response status code is " + statusCode);
		assertHttpStatus(testContext.getResponse(), statusCode);
	}

	/**
	 * Fetch the place created by the previous Add Place call and verify its name.
	 * Reads place_id from the last response, then calls the given endpoint with GET.
	 *
	 * @param name        expected place name, e.g. "Test Place 1"
	 * @param apiEndPoint endpoint name from GlobalEnum used to fetch the place, e.g. "GET_PLACE"
	 * @throws IOException if Global.properties cannot be read
	 */
	@Then("the place created maps to {string} using {string}")
	public void thePlaceCreatedMapsToUsing(String name, String apiEndPoint) throws IOException {
		testContext.setPlaceId(getJsonPath(testContext.getResponse(), "place_id"));

		logInformation("Add place_id to the request specification for the next API call");
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .queryParam("place_id", testContext.getPlaceId()));

		logInformation("Get the created place " + testContext.getPlaceId() + " with " + apiEndPoint);
		userCallsWithHttpRequest(apiEndPoint, "GET");
		assertHttpOk(testContext.getResponse());

		logInformation("Verify that the place name matches the expected value");
		assertEquals("Verify that the place name matches", name, getJsonPath(testContext.getResponse(), "name"));
	}

	/**
	 * Build the Delete Place body for the place_id set by the @DeletePlace hook (Hooks.createPlaceForDelete).
	 */
	@Given("Delete Place payload for the created place")
	public void deletePlacePayloadForTheCreatedPlace() throws IOException {
		TestDataBuilder testDataBuilder = new TestDataBuilder();
		logInformation("Build Delete Place payload for place " + testContext.getPlaceId());
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .body(testDataBuilder.deletePlacePayload(testContext.getPlaceId())));
	}

	@Then("{string} in response body is {string}")
	public void inResponseBodyIs(String key, String expectedValue) {
		logInformation("Verify that the response body contains " + key + " = " + expectedValue);
		assertEquals("Verify that the response body contains " + key, expectedValue,
		             getJsonPath(testContext.getResponse(), key));
	}

	/**
	 * Fetch the place deleted earlier in the scenario and verify the API returns 404. Reads place_id from
	 * TestContext (set by the @DeletePlace hook) and calls the given endpoint with GET.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum used to fetch the place, e.g. "GET_PLACE"
	 * @throws IOException if Global.properties cannot be read
	 */
	@Then("the deleted place can no longer be fetched using {string}")
	public void theDeletedPlaceCanNoLongerBeFetchedUsing(String apiEndPoint) throws IOException {
		logInformation("Add place_id to the request specification for the next API call");
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .queryParam("place_id", testContext.getPlaceId()));

		logInformation("Get the deleted place " + testContext.getPlaceId() + " with " + apiEndPoint);
		userCallsWithHttpRequest(apiEndPoint, "GET");

		logInformation("Verify the deleted place is not found");
		assertHttpNotFound(testContext.getResponse());
	}
}