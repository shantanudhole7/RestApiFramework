package StepDefinition;

import Resources.GlobalEnum;
import Resources.TestDataBuilder;
import Resources.Utils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;

import static Assertion.Assertions.assertEquals;
import static Assertion.Assertions.assertHttpOk;
import static Logger.Log.logInformation;
import static io.restassured.RestAssured.given;

/**
 * Step definitions for the Place API feature.
 */
public class StepDefinition extends Utils {

	private RequestSpecification requestSpecification;
	private Response response;
	private String placeId;

	@Given("Add Place payload with {string} {string} {string} {string} {string} {string}")
	public void addPlacePayloadWith(String name, String address, String phoneNumber, String language, String website,
	                                String types) throws IOException {
		TestDataBuilder testDataBuilder = new TestDataBuilder();
		logInformation("Build Add Place payload for '" + name + "'");
		requestSpecification = given().spec(defaultRequestSpecification())
		                              .body(testDataBuilder.addPlacePayload(name, address, phoneNumber, language, website, types));
	}

	/**
	 * Send the prepared request to the given endpoint and check the response against the shared JSON response spec.
	 * Also called by other steps, e.g. to fetch a created place with GET.
	 *
	 * @param apiEndPoint endpoint name from GlobalEnum, e.g. "ADD_PLACE"
	 * @param method      HTTP method: "POST" or "GET"
	 * @throws IllegalArgumentException if the HTTP method is not supported
	 */
	@When("User calls {string} with {string} http request")
	public void userCallsWithHttpRequest(String apiEndPoint, String method) {
		String resource = GlobalEnum.valueOf(apiEndPoint)
		                            .getResource();
		logInformation("User calls '" + apiEndPoint + "' with '" + method + "' http request");

		if (method.equalsIgnoreCase("Post")) {
			response = requestSpecification.when()
			                               .post(resource)
			                               .then()
			                               .spec(defaultResponseSpecification())
			                               .extract()
			                               .response();
		} else if (method.equalsIgnoreCase("Get")) {
			response = requestSpecification.when()
			                               .get(resource)
			                               .then()
			                               .spec(defaultResponseSpecification())
			                               .extract()
			                               .response();
		} else {
			throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		}
	}

	@Then("the API call succeeds with status code {int}")
	public void theApiCallSucceedsWithStatusCode(Integer statusCode) {
		logInformation("Verify the response status code is " + statusCode);
		assertEquals("Verify response status code", statusCode, response.statusCode());
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
		placeId = getJsonPath(response, "place_id");

		logInformation("Add place_id to the request specification for the next API call");
		requestSpecification = given().spec(defaultRequestSpecification())
		                              .queryParam("place_id", placeId);

		logInformation("Get the created place " + placeId + " with " + apiEndPoint);
		userCallsWithHttpRequest(apiEndPoint, "GET");
		assertHttpOk(response);

		logInformation("Verify that the place name matches the expected value");
		assertEquals("Verify that the place name matches", name, getJsonPath(response, "name"));
	}
}