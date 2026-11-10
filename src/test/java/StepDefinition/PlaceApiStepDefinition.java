package StepDefinition;

import Api.ApiClient;
import ApiDataBuilder.PlaceApiDataBuilder;
import Context.TestContext;
import Resources.GlobalEnum;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.Method;

import java.io.IOException;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpNotFound;
import static Assertion.Assertions.assertHttpOk;
import static Logger.Log.logInformation;
import static Specification.Specifications.defaultRequestSpecification;
import static Utilities.ResponseUtils.getJsonPath;
import static io.restassured.RestAssured.given;

/**
 * Step definitions for the Place API feature.
 */
public class PlaceApiStepDefinition {

	private final TestContext testContext;
	private final ApiClient apiClient;

	/**
	 * Cucumber injects the scenario's shared state (cucumber-picocontainer).
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 * @param apiClient   client to send prepared requests to the Place API and check the responses
	 */
	public PlaceApiStepDefinition(TestContext testContext, ApiClient apiClient) {
		this.testContext = testContext;
		this.apiClient = apiClient;
	}

	@Given("Add Place payload with {string} {string} {string} {string} {string} {string}")
	public void addPlacePayloadWith(String name, String address, String phoneNumber, String language, String website,
	                                String types) throws IOException {
		PlaceApiDataBuilder placeApiDataBuilder = new PlaceApiDataBuilder();
		logInformation("Build Add Place payload for '" + name + "'");
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .body(placeApiDataBuilder.addPlacePayload(name, address, phoneNumber,
		                                                                                     language, website,
		                                                                                     types)));
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
		testContext.setPlaceId(getJsonPath(testContext.getResponse(), GlobalEnum.ADD_PLACE.getCreatedIdPath()));

		logInformation("Add place_id to the request specification for the next API call");
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .queryParam("place_id", testContext.getPlaceId()));

		logInformation("Get the created place " + testContext.getPlaceId() + " with " + apiEndPoint);
		apiClient.send(GlobalEnum.valueOf(apiEndPoint), Method.GET);
		assertHttpOk(testContext.getResponse());

		logInformation("Verify that the place name matches the expected value");
		assertEquals("Verify that the place name matches", name, getJsonPath(testContext.getResponse(), "name"));
	}

	/**
	 * Build the Delete Place body for the place_id set by the @DeletePlace hook (Hooks.createPlaceForDelete).
	 */
	@Given("Delete Place payload for the created place")
	public void deletePlacePayloadForTheCreatedPlace() throws IOException {
		PlaceApiDataBuilder placeApiDataBuilder = new PlaceApiDataBuilder();
		logInformation("Build Delete Place payload for place " + testContext.getPlaceId());
		String deletePlacePayload = placeApiDataBuilder.deletePlacePayload(testContext.getPlaceId());
		testContext.setRequestSpecification(given().spec(defaultRequestSpecification())
		                                           .body(deletePlacePayload));
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
		apiClient.send(GlobalEnum.valueOf(apiEndPoint), Method.GET);

		logInformation("Verify the deleted place is not found");
		assertHttpNotFound(testContext.getResponse());
	}
}