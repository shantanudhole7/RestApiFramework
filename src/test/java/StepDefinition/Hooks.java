package StepDefinition;

import Api.PlaceApi;
import Context.TestContext;
import Resources.TestDataBuilder;
import io.cucumber.java.Before;
import io.restassured.response.Response;

import java.io.IOException;

import static Assertion.Assertions.assertHttpOk;
import static Logger.Log.logInformation;

/**
 * Cucumber hooks: setup that runs before scenarios, selected by tag.
 */
public class Hooks {

	private final TestContext testContext;

	/**
	 * Cucumber injects the same TestContext the scenario's steps use (cucumber-picocontainer),
	 * so data created here is visible to the steps.
	 *
	 * @param testContext state shared by the steps and hooks of the current scenario
	 */
	public Hooks(TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Create a place before each Delete Place scenario and keep its place_id, so every Delete scenario
	 * deletes a place of its own and can run alone, in any order, or as a retry.
	 *
	 * @throws IOException if Global.properties cannot be read
	 */
	@Before("@DeletePlace")
	public void createPlaceForDelete() throws IOException {
		logInformation("Hook: create a place for the Delete Place scenario");
		PlaceApi placeApi = new PlaceApi();
		TestDataBuilder testDataBuilder = new TestDataBuilder();

		Response response = placeApi.addPlace(testDataBuilder.defaultAddPlacePayload());
		String placeId = placeApi.getJsonPath(response, "place_id");
		if (placeId != null) {
			// Track before the status check, so the place is cleaned up even if the check fails
			testContext.addCreatedPlaceId(placeId);
		}
		assertHttpOk(response);
		testContext.setPlaceId(placeId);
	}
}